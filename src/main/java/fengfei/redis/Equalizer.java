package fengfei.redis;

import java.util.Map;

import org.apache.commons.pool.impl.GenericObjectPool;

import fengfei.redis.slice.Slice;

public interface Equalizer {
	/**
	 * 
	 * @param key
	 * @param size
	 *            slice size
	 * @return
	 */
	Slice get(String key);

	/**
	 * add ext-map
	 * 
	 * @param redisSliceMap
	 */
	void mapSlice(Map<Long, Slice> redisSliceMap);

	/**
	 * add slice
	 * 
	 * @param id
	 *            slice id
	 * @param master
	 *            host:port
	 * @param slaves
	 *            [host1:port, host2:port]
	 */
	void addSlice(long id, String master, String... slaves);

	/**
	 * seconds,default 60s
	 * 
	 * @param timeout
	 */
	void setTimeout(int timeout);

	/**
	 * default:
	 * 
	 * <pre>
	 * GenericObjectPool.Config config = new GenericObjectPool.Config();
	 * config.maxActive = 10;
	 * config.maxIdle = 10;
	 * config.minIdle = 2;
	 * config.maxWait = 60000;
	 * config.testOnBorrow = true;
	 * config.whenExhaustedAction = GenericObjectPool.WHEN_EXHAUSTED_FAIL;
	 * </pre>
	 * 
	 * @param config
	 */
	void setPoolConfig(GenericObjectPool.Config config);

	/**
	 * default:
	 * 
	 * <pre>
	 * HashPlotter
	 * </pre>
	 * 
	 * @param plotter
	 */
	void setPlotter(Plotter plotter);

	Map<Long, Slice> getSliceMap();
}
