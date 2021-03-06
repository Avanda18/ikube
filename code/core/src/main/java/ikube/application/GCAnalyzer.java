package ikube.application;

import com.sun.management.GarbageCollectorMXBean;
import ikube.IConstants;
import ikube.analytics.IAnalyticsService;
import ikube.scanner.Scanner;
import ikube.toolkit.THREAD;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.management.*;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import java.io.IOException;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.ThreadMXBean;
import java.util.*;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 23-10-2014
 */
@SuppressWarnings({"SpringJavaAutowiredMembersInspection", "unchecked"})
public class GCAnalyzer {

    static final Logger LOGGER = LoggerFactory.getLogger(GCAnalyzer.class);

    static final String EDEN_SPACE = "PS Eden Space"; // New object, and stack
    static final String PERM_GEN = "PS Perm Gen"; // Class loading area
    static final String OLD_GEN = "PS Old Gen"; // Tenured space, permanent

    /**
     * The memory areas that we will monitor.
     */
    static final String[] MEMORY_BLOCKS = {EDEN_SPACE, PERM_GEN, OLD_GEN};

    /**
     * This object normalizes the snapshots, aggregating them into snapshots of one minute.
     */
    GCSmoother gcSmoother;

    /**
     * This bean will generate the forecasts from the data collected in the collectors.
     */
    @Autowired
    IAnalyticsService analyticsService;

    /**
     * A map of JMX connectors keyed by the url built from the address and the port. We keep a reference to
     * these so we can poll them to see if they are still alive, and disconnect from them when the collector is
     * discarded.
     */
    Map<String, JMXConnector> gcConnectorMap;

    /**
     * A map of all the collectors keyed by the url built from the address and the port.
     */
    Map<String, List<GCCollector>> gcCollectorMap;

    GCAnalyzer() {
        gcSmoother = new GCSmoother();
        gcConnectorMap = new HashMap<>();
        gcCollectorMap = new HashMap<>();
    }

    /**
     * This method will take an address range, in the form of '192.168.1.0/24', it will
     * scan the network, every port, and try to connect to the machines over JMX, registering
     * {@link ikube.application.GCCollector}s to each one.
     *
     * @param addressRange the range of addresses to scan on the network, in the form '192.168.1.0/24'
     */
    public void registerCollectors(final String addressRange) {
        THREAD.submit("register-collectors", new Runnable() {
            public void run() {
                THREAD.sleep(30000);
                List<String> addressesAndPorts = Scanner.scan(addressRange, 60000, Boolean.FALSE, Boolean.TRUE);
                // We'll wait a bit for the scanner to finish completely
                THREAD.sleep(10000);
                LOGGER.info("Addresses : " + addressesAndPorts.size());
                for (final String addressAndPort : addressesAndPorts) {
                    LOGGER.info("    : " + addressAndPort);
                }
                for (final String addressAndPort : addressesAndPorts) {
                    String[] splitAddressAndPort = StringUtils.split(addressAndPort, IConstants.DELIMITER_CHARACTERS);
                    try {
                        registerCollector(splitAddressAndPort[0], Integer.valueOf(splitAddressAndPort[1]));
                    } catch (final Exception e) {
                        // As we are scanning the network looking for Jvms to connect to
                        // this will happen frequently of course, so we can safely ignore it
                        LOGGER.debug("Couldn't connect to : " + addressAndPort, e);
                    }
                }
            }
        });
    }

    /**
     * This registers the collector, connecting to the remote machine, getting access to various
     * m-beans, {@link java.lang.management.ThreadMXBean}, {@link java.lang.management.OperatingSystemMXBean} and the
     * {@link com.sun.management.GarbageCollectorMXBean}s. A {@link ikube.application.GCCollector} object is instantiated
     * for each garbage collector bean and each memory area. These collectors will then gather data from their respective
     * m-beans, being notified of events and polling for the data, and hold references to snapshots of the collected
     * data.
     *
     * @param address the ip address of the remote jvm to be monitored
     * @param port    the port of the remote machine to access the jndi registry at, i.e. the rmi registry for the m-beans
     */
    public void registerCollector(final String address, final int port) throws Exception {
        String addressAndPort = getAddressAndPort(address, port);
        // We'll unregister the collector just in case
        // unregisterCollector(address, port);
        if (collectorAddressesAndPorts().contains(addressAndPort)) {
            LOGGER.warn("Already registered, first unregister the collector : " + addressAndPort);
            return;
        }
        // TODO: This should be in a retry block
        String url = buildUri(address, port);
        JMXConnector jmxConnector = getJMXConnector(url);
        MBeanServerConnection mBeanServerConnection = jmxConnector.getMBeanServerConnection();
        ThreadMXBean threadMXBean = getThreadMXBean(mBeanServerConnection);
        OperatingSystemMXBean operatingSystemMXBean = getOperatingSystemMXBean(mBeanServerConnection);
        List<GarbageCollectorMXBean> garbageCollectorMXBeans = getGarbageCollectorMXBeans(mBeanServerConnection);

        List<GCCollector> gcCollectors = new ArrayList<>();
        for (final GarbageCollectorMXBean garbageCollectorMXBean : garbageCollectorMXBeans) {
            ObjectName gcObjectName = garbageCollectorMXBean.getObjectName();
            for (final String memoryBlock : MEMORY_BLOCKS) {
                final GCCollector gcCollector = new GCCollector(memoryBlock, threadMXBean, operatingSystemMXBean, garbageCollectorMXBean);
                class GCCollectorNotificationListener implements NotificationListener {
                    public void handleNotification(final Notification notification, final Object handback) {
                        gcCollector.getGcSnapshot();
                    }
                }
                mBeanServerConnection.addNotificationListener(gcObjectName, new GCCollectorNotificationListener(), null, null);
                gcCollectors.add(gcCollector);
            }
        }

        gcConnectorMap.put(addressAndPort, jmxConnector);
        gcCollectorMap.put(addressAndPort, gcCollectors);
    }

    /**
     * This method will unregister the {@link ikube.application.GCCollector} from the remote machine, terminating
     * the connection, and destroying any data that has been collected up to that point.
     *
     * @param address the address of the target jvm get get the garbage collection and system data for
     * @param port    the port of the remote machine to access the jndi registry at, i.e. the rmi registry for the m-beans
     */
    public void unregisterCollector(final String address, final int port) {
        gcCollectorMap.remove(getAddressAndPort(address, port));
        JMXConnector jmxConnector = gcConnectorMap.remove(getAddressAndPort(address, port));
        if (jmxConnector != null) {
            try {
                jmxConnector.close();
            } catch (final IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * This method will return all the addresses and ports that the system is currently connect
     * to and monitoring for memory usage.
     *
     * @return the list of addresses and ports currently being monitored by the system
     */
    public List<String> collectorAddressesAndPorts() {
        List<String> addresses = new ArrayList<>();
        for (final Map.Entry<String, JMXConnector> mapEntry : gcConnectorMap.entrySet()) {
            addresses.add(mapEntry.getKey());
        }
        return addresses;
    }

    /**
     * This method accesses all the data for all the snapshots in all the collectors. Normalizes the data to be
     * consistent over equal time periods using the {@link ikube.application.GCSmoother}. Then creates a matrix of
     * matrices, ...
     *
     * @param address the address of the target jvm get get the garbage collection and system data for
     * @return a vector of matrices of the snapshot data of the target jvm
     */
    public Object[][][] getGcData(final String address, final int port) {
        List<GCCollector> gcCollectors = gcCollectorMap.get(getAddressAndPort(address, port));
        if (gcCollectors == null) {
            return null;
        }
        Object[][][] gcTimeSeriesMatrices = new Object[gcCollectors.size()][][];
        for (int i = 0; i < gcCollectors.size(); i++) {
            GCCollector gcCollector = gcCollectors.get(i);
            List<GCSnapshot> smoothedGcSnapshots = gcSmoother.getSmoothedSnapshots(gcCollector.getGcSnapshots());
            Object[][] gcTimeSeriesMatrix = new Object[smoothedGcSnapshots.size()][];
            for (int j = 0; j < smoothedGcSnapshots.size(); j++) {
                GCSnapshot smoothedGcSnapshot = smoothedGcSnapshots.get(j);

                LOGGER.info("Smooth snapshot : " + ToStringBuilder.reflectionToString(smoothedGcSnapshot));

                Object[] gcTimeSeriesVector = new Object[8];
                gcTimeSeriesVector[0] = new Date(smoothedGcSnapshot.start);
                gcTimeSeriesVector[1] = smoothedGcSnapshot.delta;
                gcTimeSeriesVector[2] = smoothedGcSnapshot.duration;
                gcTimeSeriesVector[3] = smoothedGcSnapshot.interval;
                gcTimeSeriesVector[4] = smoothedGcSnapshot.perCoreLoad;
                gcTimeSeriesVector[5] = smoothedGcSnapshot.runsPerTimeUnit;
                gcTimeSeriesVector[6] = smoothedGcSnapshot.usedToMaxRatio;
                gcTimeSeriesVector[7] = smoothedGcSnapshot.available;

                gcTimeSeriesMatrix[j] = gcTimeSeriesVector;
            }
            gcTimeSeriesMatrices[i] = gcTimeSeriesMatrix;

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Snapshots : " + gcCollector.getGcSnapshots().size());
                LOGGER.debug("Smooth snapshots : " + smoothedGcSnapshots.size());
            }
        }
        return gcTimeSeriesMatrices;
    }

    JMXConnector getJMXConnector(final String url) throws IOException {
        JMXServiceURL jmxServiceUrl = new JMXServiceURL(url);
        return JMXConnectorFactory.connect(jmxServiceUrl);
    }

    OperatingSystemMXBean getOperatingSystemMXBean(final MBeanServerConnection mBeanServerConnection) throws MalformedObjectNameException, IOException {
        ObjectInstance osInstance = mBeanServerConnection.queryMBeans(new ObjectName("*:type=OperatingSystem,*"), null).iterator().next();
        return JMX.newMXBeanProxy(mBeanServerConnection, osInstance.getObjectName(), OperatingSystemMXBean.class, false);
    }

    ThreadMXBean getThreadMXBean(final MBeanServerConnection mBeanServerConnection) throws MalformedObjectNameException, IOException {
        ObjectInstance threadInstance = mBeanServerConnection.queryMBeans(new ObjectName("*:type=Threading,*"), null).iterator().next();
        return JMX.newMXBeanProxy(mBeanServerConnection, threadInstance.getObjectName(), ThreadMXBean.class, false);
    }

    List<GarbageCollectorMXBean> getGarbageCollectorMXBeans(final MBeanServerConnection mBeanServerConnection) throws MalformedObjectNameException, IOException {
        List<GarbageCollectorMXBean> garbageCollectorMXBeans = new ArrayList<>();
        Set<ObjectInstance> gcInstances = mBeanServerConnection.queryMBeans(new ObjectName("*:type=GarbageCollector,*"), null);
        for (final ObjectInstance gcInstance : gcInstances) {
            // Create one collector per garbage collector and memory block, so
            // the total collectors will be n(gcs) * m(memory blocks), about 6 then
            ObjectName gcObjectName = gcInstance.getObjectName();
            GarbageCollectorMXBean garbageCollectorMXBean = JMX.newMXBeanProxy(mBeanServerConnection, gcObjectName, GarbageCollectorMXBean.class, true);
            garbageCollectorMXBeans.add(garbageCollectorMXBean);
        }
        return garbageCollectorMXBeans;
    }

    String getAddressAndPort(final String address, final int port) {
        return address + ":" + port;
    }

    @SuppressWarnings("StringBufferReplaceableByString")
    String buildUri(final String address, final int port) {
        // Could we switch to JMXMP, seems nicer!
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder
                .append("service:jmx:rmi:///jndi/rmi://")
                .append(address)
                .append(":")
                .append(port)
                .append("/jmxrmi");
        return stringBuilder.toString();
    }

    @SuppressWarnings("UnusedDeclaration")
    void printMBeans(final MBeanServerConnection mBeanServerConnection) throws IOException {
        Set<ObjectName> objectNames = mBeanServerConnection.queryNames(null, null);
        for (final ObjectName objectName : objectNames) {
            LOGGER.info("Object name : " + objectName);
        }
    }

}