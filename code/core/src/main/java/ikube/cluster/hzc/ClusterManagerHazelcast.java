package ikube.cluster.hzc;

import ikube.IConstants;
import ikube.cluster.AClusterManager;
import ikube.cluster.IClusterManager;
import ikube.cluster.listener.hzc.DeleteListener;
import ikube.cluster.listener.hzc.StartListener;
import ikube.cluster.listener.hzc.StopListener;
import ikube.model.Action;
import ikube.model.IndexContext;
import ikube.model.Server;
import ikube.service.IMonitorService;
import ikube.toolkit.ThreadUtilities;
import ikube.toolkit.UriUtilities;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.ILock;
import com.hazelcast.core.MessageListener;

/**
 * @see IClusterManager
 * @author Michael Couck
 * @since 15.07.12
 * @version 01.00
 */
public class ClusterManagerHazelcast extends AClusterManager {

	/** This listener on Hazelcast will listen for start events, that indicate that */
	@Autowired
	private StartListener startListener;
	/** This listener will either stop one job or destroy the thread pool for all jobs. */
	@Autowired
	private StopListener stopListener;
	/** Ths listener will delete the index and the backup directory on the file system. */
	@Autowired
	private DeleteListener deleteListener;
	/** This class had methods to query the state of the contexts. */
	@Autowired
	private IMonitorService monitorService;

	private transient Server server;

	@SuppressWarnings("unchecked")
	public void initialize() {
		ip = UriUtilities.getIp();
		address = ip + "-" + Hazelcast.getCluster().getLocalMember().getInetSocketAddress().getPort();
		logger.info("Cluster manager : " + ip + ", " + address);
		addListeners(startListener, stopListener, deleteListener);
	}

	private void addListeners(final MessageListener<Object>... listeners) {
		for (final MessageListener<Object> listener : listeners) {
			if (listener == null) {
				continue;
			}
			Hazelcast.getTopic(IConstants.TOPIC).addMessageListener(listener);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public synchronized boolean lock(String name) {
		try {
			ILock lock = Hazelcast.getLock(name);
			boolean gotLock = false;
			try {
				gotLock = lock.tryLock(250, TimeUnit.MILLISECONDS);
			} catch (InterruptedException e) {
				logger.error("Exception trying for the lock : ", e);
			}
			return gotLock;
		} finally {
			notifyAll();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public synchronized boolean unlock(String name) {
		try {
			ILock lock = Hazelcast.getLock(name);
			lock.unlock();
			return true;
		} finally {
			notifyAll();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean anyWorking() {
		Map<String, Server> servers = getServers();
		for (Map.Entry<String, Server> mapEntry : servers.entrySet()) {
			Server server = mapEntry.getValue();
			if (server.isWorking()) {
				return true;
			}
		}
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean anyWorking(String indexName) {
		Map<String, Server> servers = getServers();
		for (Map.Entry<String, Server> mapEntry : servers.entrySet()) {
			Server server = mapEntry.getValue();
			if (!server.isWorking() || server.getActions() == null) {
				continue;
			}
			for (Action action : server.getActions()) {
				if (indexName.equals(action.getIndexName())) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public synchronized Action startWorking(String actionName, String indexName, String indexableName) {
		Hazelcast.getTransaction().begin();
		Action action = null;
		Server server = getServer();
		try {
			action = getAction(actionName, indexName, indexableName);
			server.getActions().add(action);
			Hazelcast.getMap(IConstants.IKUBE).put(server.getAddress(), server);
			Hazelcast.getTransaction().commit();
		} catch (Exception e) {
			logger.error("Exception starting action : " + actionName + ", " + indexName + ", " + indexableName, e);
			Hazelcast.getTransaction().rollback();
			server.getActions().remove(action);
			action = null;
		} finally {
			notifyAll();
		}
		return action;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public synchronized void stopWorking(final Action action) {
		try {
			stopWorking(action, 0);
		} finally {
			notifyAll();
		}
	}

	private synchronized void stopWorking(final Action action, final int retry) {
		if (action == null) {
			logger.warn("Action null : " + action);
			return;
		}
		if (retry >= IConstants.MAX_RETRY_CLUSTER_REMOVE) {
			logger.info("Retried to remove the action, failed : " + retry + ", action : " + action + ", actions : " + server.getActions());
			return;
		}
		Hazelcast.getTransaction().begin();
		boolean removedAndComitted = false;
		Server server = getServer();
		Action toRemoveAction = null;
		try {
			action.setEndTime(new Timestamp(System.currentTimeMillis()));
			action.setDuration(action.getEndTime().getTime() - action.getStartTime().getTime());
			Iterator<Action> iterator = server.getActions().iterator();
			// We must iterate over the actions and remove the one by id because the
			// equals is modified by Hazelcast it seems
			boolean removedFromServerActions = false;
			while (iterator.hasNext()) {
				toRemoveAction = iterator.next();
				if (toRemoveAction.getId() == action.getId()) {
					iterator.remove();
					removedFromServerActions = true;
					break;
				}
			}
			Hazelcast.getMap(IConstants.IKUBE).put(server.getAddress(), server);
			// Commit the grid because the database is not as important as the cluster
			Hazelcast.getTransaction().commit();
			removedAndComitted = removedFromServerActions;
			if (dataBase.find(Action.class, action.getId()) != null) {
				dataBase.merge(action);
			}
		} catch (ConcurrentModificationException e) {
			logger.warn("Concurrent error, will retry : " + e.getMessage());
		} catch (Exception e) {
			logger.error("Exception stopping action : " + action, e);
			logger.error("Removed action not comitted : " + toRemoveAction);
			Hazelcast.getTransaction().rollback();
		} finally {
			if (!removedAndComitted) {
				ThreadUtilities.sleep(1000);
				logger.debug("Retrying to remove the action : " + retry + ", " + server.getIp() + ", " + action + ", "
						+ server.getActions());
				stopWorking(action, retry + 1);
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Map<String, Server> getServers() {
		return Hazelcast.getMap(IConstants.IKUBE);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@SuppressWarnings("rawtypes")
	public Server getServer() {
		try {
			Server server = (Server) Hazelcast.getMap(IConstants.IKUBE).get(address);
			if (server != null) {
				// We set the instance server to the one in
				// Hazelcast so if Hazelcast drops the server we have a
				// copy to replace it
				this.server = server;
			} else {
				// First check if the instance server is still active
				if (this.server != null) {
					server = this.server;
					logger.warn("Server dropped from Hazelcast! Re-inserting into the grid... " + server);
					try {
						Hazelcast.getTransaction().begin();
						Hazelcast.getMap(IConstants.IKUBE).put(server.getAddress(), server);
						Hazelcast.getTransaction().commit();
					} catch (Exception e) {
						logger.error("Exception re-injecting the server in the grid : " + server.getAddress(), e);
						Hazelcast.getTransaction().rollback();
					}
				} else {
					server = new Server();
					server.setIp(ip);
					server.setAddress(address);
					server.setId(System.currentTimeMillis());
					logger.info("Server null, creating new one : " + server);
				}
			}
			server.setAge(System.currentTimeMillis());

			Collection<IndexContext> collection = monitorService.getIndexContexts().values();
			List<IndexContext> indexContexts = new ArrayList<IndexContext>(collection);
			server.setIndexContexts(indexContexts);

			return server;
		} catch (Exception e) {
			logger.error("Exception getting the server : ", e);
		}
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void sendMessage(Serializable serializable) {
		Hazelcast.getTopic(IConstants.TOPIC).publish(serializable);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@SuppressWarnings("unchecked")
	public <T> T getObject(final Object key) {
		return (T) Hazelcast.getMap(IConstants.IKUBE).get(key);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void putObject(final Object key, final Object value) {
		Hazelcast.getMap(IConstants.IKUBE).put(key, value);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void remove(final Object key) {
		Hazelcast.getMap(IConstants.IKUBE).remove(key);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void destroy() {
		Hazelcast.shutdownAll();
	}

}