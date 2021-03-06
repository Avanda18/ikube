package ikube.toolkit;

import ikube.IConstants;
import ikube.model.*;
import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.beanutils.Converter;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.AbstractRefreshableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Class for accessing the Spring context.
 *
 * @author Michael Couck
 * @version 01.00
 * @since 29-04-2009
 */
public final class ApplicationContextManager implements ApplicationContextAware {

    private static final Logger LOGGER;
    /**
     * The default location of the configuration files is in the ikube folder at the base of the server.
     */
    private static final String EXTERNAL_SPRING_CONFIGURATION_FILE =
            "." +
                    IConstants.SEP +
                    IConstants.IKUBE +
                    IConstants.SEP +
                    IConstants.SPRING_XML;

    private static ApplicationContext APPLICATION_CONTEXT;

    static {
        LOGGING.configure();
        LOGGER = LoggerFactory.getLogger(ApplicationContextManager.class);
        try {
            SERIALIZATION.setTransientFields(//
                    ikube.model.File.class, //
                    Url.class, //
                    Analysis.class, //
                    Search.class, //
                    Task.class, //
                    IndexableInternet.class, //
                    IndexableEmail.class, //
                    IndexableFileSystem.class, //
                    IndexableColumn.class, //
                    IndexableTable.class, //
                    IndexContext.class, //
                    ArrayList.class);
        } catch (final Exception e) {
            LOGGER.error("Exception setting the transient fields : ", e);
        }
        // We register a converter for the Bean utils so it
        // doesn't complain when the value is null
        ConvertUtils.register(new Converter() {
            @Override
            public Object convert(final Class type, final Object value) {
                return value;
            }
        }, Timestamp.class);
    }

    /**
     * System wide access to the Spring context. This method is called when Ikube is started without a server
     * i.e. in stand alone mode, or from the integration tests. Generally it will be in a server and the Spring
     * web context will handle the initialization.
     *
     * @return the Spring application context for the system
     */
    public static synchronized ApplicationContext getApplicationContext() {
        try {
            if (APPLICATION_CONTEXT == null) {
                String configFilePath = getConfigFilePath();
                if (configFilePath != null) {
                    APPLICATION_CONTEXT = getApplicationContextFilesystem(configFilePath);
                } else {
                    // Now just get the class path configuration as a default
                    LOGGER.info("Default location for configuration file : ");
                    APPLICATION_CONTEXT = getApplicationContext(IConstants.SPRING_CONFIGURATION_FILE);
                }
            }
            return APPLICATION_CONTEXT;
        } finally {
            ApplicationContextManager.class.notifyAll();
        }
    }

    /**
     * This method is called by the 'web' part of the Spring configuration, which sets the context for us.
     */
    @Override
    public void setApplicationContext(final ApplicationContext applicationContext) throws BeansException {
        if (APPLICATION_CONTEXT == null) {
            LOGGER.info("Setting the application context from the web part : " + applicationContext + ", " + applicationContext.getClass());
            APPLICATION_CONTEXT = applicationContext;
            ((AbstractApplicationContext) APPLICATION_CONTEXT).registerShutdownHook();
            File configDirectory = null;
            File consoleOutputFile = null;
            InputStream inputStream = null;
            try {
                Object ikubeConfigurationPathProperty = System.getProperty(IConstants.IKUBE_CONFIGURATION);
                // First try the configuration property
                if (ikubeConfigurationPathProperty == null) {
                    configDirectory = new File(IConstants.IKUBE_DIRECTORY);
                } else {
                    configDirectory = new File(ikubeConfigurationPathProperty.toString());
                }
                consoleOutputFile = FILE.findFileRecursively(configDirectory, "console");

                String version = VERSION.version();
                String timestamp = VERSION.timestamp();
                inputStream = new FileInputStream(consoleOutputFile);
                List<String> lines = IOUtils.readLines(inputStream);
                for (final String line : lines) {
                    String formatted = String.format(line, version, timestamp);
                    System.out.println(formatted);
                }
            } catch (final Exception e) {
                LOGGER.error("Error reading the console file : " + configDirectory + ", " + consoleOutputFile, e);
            } finally {
                IOUtils.closeQuietly(inputStream);
            }
        } else {
            LOGGER.info("Application context already loaded : " + APPLICATION_CONTEXT);
        }
    }

    /**
     * This method will return the configuration path on the local file system. If the user specifies the system
     * property 'ikube-configuration', then this will be the starting point. If this property is not set then we will
     * look in the './ikube' directory for the configuration.
     *
     * @return the absolute path to the configuration directory for the system
     */
    public static String getConfigFilePath() {
        File configFile = null;
        Object ikubeConfigurationPathProperty = System.getProperty(IConstants.IKUBE_CONFIGURATION);
        LOGGER.info("Configuration property file : " + ikubeConfigurationPathProperty);
        // First try the configuration property
        if (ikubeConfigurationPathProperty != null) {
            configFile = new File(ikubeConfigurationPathProperty.toString(), IConstants.SPRING_XML);
        }
        // See if there is a configuration file at the base of where the Jvm was started
        if (configFile == null || !configFile.isFile()) {
            configFile = new File(EXTERNAL_SPRING_CONFIGURATION_FILE);
        }
        if (configFile.isFile()) {
            // From the file system
            String configFilePath = FILE.cleanFilePath(configFile.getAbsolutePath());
            // Why did I do this? Because Spring context wants it!
            configFilePath = "file:" + configFilePath;
            LOGGER.info("Configuration file path : " + configFilePath);
            return configFilePath;
        }
        throw new RuntimeException("No configuration found : " + ikubeConfigurationPathProperty + ", " + configFile);
    }

    /**
     * Convenience method to get the bean type from the class.
     *
     * @param <T>   the type of bean to return
     * @param klass the class of the bean
     * @return the bean with the specified class
     */
    public static synchronized <T> T getBean(final Class<T> klass) {
        try {
            return getApplicationContext().getBean(klass);
        } finally {
            ApplicationContextManager.class.notifyAll();
        }
    }

    /**
     * Convenience method to get the bean type from the bean name. Note that this method is not
     * type checked and there is a distinct possibility for a class cast exception.
     *
     * @param name the name of the bean
     * @return the bean with the specified name
     */
    @SuppressWarnings("unchecked")
    public static synchronized <T> T getBean(final String name) {
        try {
            return (T) getApplicationContext().getBean(name);
        } finally {
            ApplicationContextManager.class.notifyAll();
        }
    }

    /**
     * Access to all the beans of a particular type.
     *
     * @param <T>   the expected type
     * @param klass the class of the beans
     * @return a map of bean names and beans of type T
     */
    public static synchronized <T> Map<String, T> getBeans(final Class<T> klass) {
        try {
            return getApplicationContext().getBeansOfType(klass);
        } finally {
            ApplicationContextManager.class.notifyAll();
        }
    }

    /**
     * This method will register a bean dynamically. There are no properties set in the bean and
     * the default constructor will probably be used by Spring.
     *
     * @param name          the name of the bean, unique in the application context, i.e. the id
     * @param beanClassName the class type of the bean to create
     * @return the newly created bean, as a singleton, for convenience
     */
    public static synchronized <T> T setBean(final String name, final String beanClassName) {
        ApplicationContext applicationContext = getApplicationContext();
        if (AbstractRefreshableApplicationContext.class.isAssignableFrom(applicationContext.getClass())) {
            BeanFactory beanFactory = ((AbstractRefreshableApplicationContext) applicationContext).getBeanFactory();
            if (DefaultListableBeanFactory.class.isAssignableFrom(beanFactory.getClass())) {
                BeanDefinition beanDefinition = new GenericBeanDefinition();
                beanDefinition.setBeanClassName(beanClassName);
                ((DefaultListableBeanFactory) beanFactory).registerBeanDefinition(name, beanDefinition);
            }
        }
        return getBean(name);
    }

    /**
     * Instantiates the application context using all the configuration files in the parameter list.
     *
     * @param configLocations the locations of the configuration files
     * @return the merged application context for all the configuration files
     */
    public static synchronized ApplicationContext getApplicationContextFilesystem(final String... configLocations) {
        try {
            if (APPLICATION_CONTEXT == null) {
                LOGGER.info("Loading the application context with configuration : " + Arrays.deepToString(configLocations));
                APPLICATION_CONTEXT = new FileSystemXmlApplicationContext(configLocations);
                ((AbstractApplicationContext) APPLICATION_CONTEXT).registerShutdownHook();
                LOGGER.info("Loaded the application context with configuration : " + Arrays.deepToString(configLocations));
            }
            return APPLICATION_CONTEXT;
        } finally {
            ApplicationContextManager.class.notifyAll();
        }
    }

    /**
     * Instantiates the application context using all the configuration files in the parameter list.
     *
     * @param configLocations the locations of the configuration files
     * @return the merged application context for all the configuration files
     */
    public static synchronized ApplicationContext getApplicationContext(final String... configLocations) {
        try {
            if (APPLICATION_CONTEXT == null) {
                LOGGER.info("Loading the application context with configurations : " + Arrays.deepToString(configLocations));
                APPLICATION_CONTEXT = new ClassPathXmlApplicationContext(configLocations);
                ((AbstractApplicationContext) APPLICATION_CONTEXT).registerShutdownHook();
                LOGGER.info("Loaded the application context with configurations : " + Arrays.deepToString(configLocations));
            }
            return APPLICATION_CONTEXT;
        } finally {
            ApplicationContextManager.class.notifyAll();
        }
    }

    /**
     * Closes the application context.
     */
    public static synchronized void closeApplicationContext() {
        try {
            if (APPLICATION_CONTEXT != null) {
                ((AbstractApplicationContext) APPLICATION_CONTEXT).close();
                APPLICATION_CONTEXT = null;
            }
        } finally {
            ApplicationContextManager.class.notifyAll();
        }
    }

    public void initialize() {
        // What to do?
    }

}