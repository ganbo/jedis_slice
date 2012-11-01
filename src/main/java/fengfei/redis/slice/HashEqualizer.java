package fengfei.redis.slice;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.pool.impl.GenericObjectPool.Config;

import redis.clients.util.Hashing;

import fengfei.redis.Equalizer;
import fengfei.redis.Plotter;

/**
 * key -> hash % size->slice
 * 
 * 
 */
public class HashEqualizer extends AbstractEqualizer implements Equalizer {

	protected Hashing hashed = Hashing.MD5;
	protected Map<Long, Slice> sliceMap = new ConcurrentHashMap<>();

	public HashEqualizer() {

	}

	public HashEqualizer(int timeout, Config config, Plotter plotter) {
		super(timeout, config, plotter);
	}

	@Override
	public Slice get(String key) {
		int size=getSliceMap().size();
		long sk = Math.abs(hashed.hash(key) % size);
		return sliceMap.get(sk);
	}

	@Override
	public void mapSlice(Map<Long, Slice> redisSliceMap) {
		sliceMap = new ConcurrentHashMap<>(redisSliceMap);
	}

	@Override
	public Map<Long, Slice> getSliceMap() {
		return sliceMap;
	}
}
