package fengfei.redis.slice;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.pool.impl.GenericObjectPool.Config;

import fengfei.redis.Equalizer;
import fengfei.redis.Plotter;

/**
 * key-> long % size->slice
 * 
 * 
 */
public class LongModuleEqualizer extends AbstractEqualizer implements Equalizer {

	Map<Long, Slice> sliceMap = new ConcurrentHashMap<>();

	public LongModuleEqualizer() {
	}

	public LongModuleEqualizer(int timeout, Config config, Plotter plotter,
			Map<Long, Slice> sliceMap) {
		super(timeout, config, plotter);
		this.sliceMap = sliceMap;
	}

	@Override
	public Slice get(String key) {
		int size = getSliceMap().size();
		long mod = Long.parseLong(key);
		long sk = Math.abs(mod % size);
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
