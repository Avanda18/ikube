package ikube.cluster.hzc;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import ikube.IConstants;
import ikube.database.IDataBase;
import ikube.model.Action;
import ikube.model.Server;
import ikube.toolkit.ThreadUtilities;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Future;

import mockit.Cascading;
import mockit.Deencapsulation;
import mockit.Mock;
import mockit.MockClass;
import mockit.Mockit;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.IMap;

public class ClusterManagerHazelcastTest {

	protected Logger logger = LoggerFactory.getLogger(this.getClass());

	private String actionName = "actionName";
	private String indexName = "indexName";
	private String indexableName = "indexableName";

	@Cascading
	private IDataBase dataBase;
	private ClusterManagerHazelcast clusterManagerHazelcast;

	@Before
	public void before() {
		Mockit.setUpMocks();
		Mockit.setUpMocks(HazelcastMock.class);
		clusterManagerHazelcast = new ClusterManagerHazelcast();
		clusterManagerHazelcast.initialize();
	}

	@After
	public void after() {
		Mockit.tearDownMocks();
		clusterManagerHazelcast.unlock(IConstants.IKUBE);
	}

	@Test
	public void lock() throws Exception {
		boolean gotLock = clusterManagerHazelcast.lock(IConstants.IKUBE);
		assertTrue(gotLock);
		Thread thread = new Thread(new Runnable() {
			public void run() {
				boolean gotLock = clusterManagerHazelcast.lock(IConstants.IKUBE);
				assertFalse(gotLock);
			}
		});
		thread.start();
		thread.join();
	}

	@Test
	public void unlock() throws Exception {
		boolean gotLock = clusterManagerHazelcast.lock(IConstants.IKUBE);
		assertTrue(gotLock);
		Thread thread = new Thread(new Runnable() {
			public void run() {
				boolean gotLock = clusterManagerHazelcast.lock(IConstants.IKUBE);
				assertFalse(gotLock);
			}
		});
		thread.start();
		Thread.sleep(1000);

		clusterManagerHazelcast.unlock(IConstants.IKUBE);
		thread = new Thread(new Runnable() {
			public void run() {
				boolean gotLock = clusterManagerHazelcast.lock(IConstants.IKUBE);
				assertTrue(gotLock);
			}
		});
		thread.start();
		thread.join();
		Thread.sleep(1000);
	}

	@MockClass(realClass = Hazelcast.class)
	public static class HazelcastMock {

		@SuppressWarnings("unchecked")
		static IMap<String, Server> servers = mock(IMap.class);

		@Mock
		@SuppressWarnings("unchecked")
		public static <K, V> IMap<K, V> getMap(String name) {
			return (IMap<K, V>) servers;
		}
	}

	Set<Map.Entry<String, Server>> entrySet = new HashSet<Map.Entry<String, Server>>();
	@SuppressWarnings("unchecked")
	Map.Entry<String, Server> entry = mock(Map.Entry.class);
	Server server = mock(Server.class);
	{
		when(server.isWorking()).thenReturn(false);
		when(entry.getValue()).thenReturn(server);
		when(HazelcastMock.servers.entrySet()).thenReturn(entrySet);
		when(HazelcastMock.servers.get(anyString())).thenReturn(server);
	}

	@Test
	public void anyWorking() {
		boolean anyworking = clusterManagerHazelcast.anyWorking();
		assertFalse(anyworking);

		entrySet.add(entry);
		anyworking = clusterManagerHazelcast.anyWorking();
		assertFalse(anyworking);

		when(server.isWorking()).thenReturn(true);
		anyworking = clusterManagerHazelcast.anyWorking();
		assertTrue(anyworking);
	}

	@Test
	public void anyWorkingIndex() {
		Deencapsulation.setField(clusterManagerHazelcast, dataBase);
		boolean anyWorkingIndex = clusterManagerHazelcast.anyWorking(indexName);
		assertFalse(anyWorkingIndex);

		Action action = mock(Action.class);
		when(action.getIndexName()).thenReturn(indexName);
		when(server.getActions()).thenReturn(Arrays.asList(action));

		entrySet.add(entry);
		when(server.isWorking()).thenReturn(true);
		anyWorkingIndex = clusterManagerHazelcast.anyWorking(indexName);
		assertTrue(anyWorkingIndex);
	}

	@Test
	public void startWorking() {
		Deencapsulation.setField(clusterManagerHazelcast, dataBase);
		Action action = clusterManagerHazelcast.startWorking(actionName, indexName, indexableName);
		assertEquals(indexName, action.getIndexName());
	}

	@Test
	public void stopWorking() {
		Deencapsulation.setField(clusterManagerHazelcast, dataBase);

		Action action = mock(Action.class);
		when(action.getStartTime()).thenReturn(new Date());
		when(action.getEndTime()).thenReturn(new Date());
		when(action.getIndexName()).thenReturn(indexName);
		when(server.getActions()).thenReturn(new ArrayList<Action>(Arrays.asList(action)));

		clusterManagerHazelcast.stopWorking(action);
		assertNotNull(action.getEndTime());

		assertEquals(0, server.getActions().size());
	}

	@Test
	public void getServerAndServers() {
		Mockit.tearDownMocks();
		Server server = clusterManagerHazelcast.getServer();
		assertNotNull(server);
		Map<String, Server> servers = clusterManagerHazelcast.getServers();
		assertNotNull(servers);
		assertEquals(1, servers.size());
	}

	@Test
	public void sendMessage() {
		// Not implemented
	}

	@Test
	public void getLocks() {
		// Not implemented
	}

	@Test
	public void threaded() {
		Hazelcast.getLock(IConstants.IKUBE).forceUnlock();
		ThreadUtilities.initialize();
		int threads = 10;
		final int iterations = 10000;
		final double sleep = 1;
		final Boolean[] locks = new Boolean[threads];
		List<Future<?>> futures = new ArrayList<Future<?>>();
		for (int i = 0; i < threads; i++) {
			final int thread = i;
			Runnable runnable = new Runnable() {
				public void run() {
					for (int i = 0; i < iterations; i++) {
						boolean lock = clusterManagerHazelcast.lock(IConstants.IKUBE);
						locks[thread] = lock;
						validate(locks);
						clusterManagerHazelcast.unlock(IConstants.IKUBE);
						locks[thread] = false;
						ThreadUtilities.sleep((long) (sleep * Math.random()));
					}
				}
			};
			Future<?> future = ThreadUtilities.submit(runnable);
			futures.add(future);
		}
		ThreadUtilities.waitForFutures(futures, Long.MAX_VALUE);
		ThreadUtilities.destroy();
	}

	private void validate(final Boolean[] locks) {
		int count = 0;
		// logger.info("Validate ; " + Arrays.deepToString(locks));
		for (Boolean lock : locks) {
			count = lock == null ? count : lock ? count + 1 : count;
		}
		assertTrue(count <= 1);
	}

}