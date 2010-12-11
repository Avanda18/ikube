package ikube.cluster.cache;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.IMap;
import com.hazelcast.query.SqlPredicate;

public class Cache implements ICache {

	protected Logger logger;
	private Map<Class<?>, Map<Long, ?>> maps;

	public void initialise() {
		logger = Logger.getLogger(this.getClass());
		maps = new HashMap<Class<?>, Map<Long, ?>>();
	}

	@Override
	public <T> int size(Class<T> klass) {
		return getMap(klass).size();
	}

	@Override
	public <T> T get(Class<T> klass, Long hash) {
		return getMap(klass).get(hash);
	}

	@Override
	public <T> void set(Class<T> klass, Long hash, T t) {
		getMap(klass).put(hash, t);
	}

	@Override
	public <T> void remove(Class<T> klass, Long hash) {
		getMap(klass).remove(hash);
	}

	@Override
	public <T> List<T> get(Class<T> klass, ICriteria<T> criteria, IAction<T> action, int size) {
		List<T> batch = new ArrayList<T>();
		Map<Long, T> map = getMap(klass);
		for (Long key : map.keySet()) {
			T t = map.get(key);
			if (criteria != null) {
				boolean evaluated = criteria.evaluate(t);
				if (!evaluated) {
					continue;
				}
			}
			if (action != null) {
				action.execute(t);
			}
			batch.add(t);
			if (batch.size() >= size) {
				break;
			}
		}
		return batch;
	}

	@Override
	public <T> void clear(Class<T> klass) {
		getMap(klass).clear();
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> T get(Class<T> klass, String sql) {
		Map<Long, T> map = getMap(klass);
		if (IMap.class.isAssignableFrom(map.getClass())) {
			Collection<Object> collection = ((IMap<Long, T>) map).values(new SqlPredicate(sql));
			if (collection.size() == 0) {
				return null;
			}
			return (T) collection.iterator().next();
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	private <T> Map<Long, T> getMap(Class<T> klass) {
		Map<Long, T> map = (Map<Long, T>) maps.get(klass);
		if (map == null) {
			map = Hazelcast.getMap(klass.getSimpleName());
			maps.put(klass, map);
		}
		return map;
	}

}
