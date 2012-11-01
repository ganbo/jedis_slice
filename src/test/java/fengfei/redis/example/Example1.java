package fengfei.redis.example;

import org.apache.commons.pool.impl.GenericObjectPool;

import fengfei.redis.Equalizer;
import fengfei.redis.RedisComand;
import fengfei.redis.slice.HashEqualizer;
import fengfei.redis.slice.LongModuleEqualizer;
import fengfei.redis.slice.LoopPlotter;
import fengfei.redis.slice.PoolableSlicedRedis;

public class Example1 {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		GenericObjectPool.Config config = new GenericObjectPool.Config();
		config.maxActive = 10;
		config.maxIdle = 10;
		config.minIdle = 2;
		config.maxWait = 60000;
		config.testOnBorrow = true;
		config.whenExhaustedAction = GenericObjectPool.WHEN_EXHAUSTED_FAIL;
		// example1(config);
		// example2(config);
		example3(config);

	}

	public static void example1(GenericObjectPool.Config config) {
		PoolableSlicedRedis redis = new PoolableSlicedRedis(
				"192.168.1.1:6379 192.168.1.1:6380", 60000,
				new HashEqualizer(), config);
		RedisComand rc = redis.createRedisCommand();

		for (int i = 0; i < 10; i++) {
			rc.set("K" + i, "V" + i);
		}
		redis.close();
	}

	public static void example2(GenericObjectPool.Config config) {
		PoolableSlicedRedis redis = new PoolableSlicedRedis(
				"192.168.1.1:6379 192.168.1.1:6380", 60000,
				new LongModuleEqualizer(), config);
		RedisComand rc = redis.createRedisCommand();
		for (int i = 0; i < 10; i++) {
			rc.set("" + i, "V" + i);
		}
		redis.close();
	}

	public static void example3(GenericObjectPool.Config config) {
//		Equalizer equalizer = new HashEqualizer();
		Equalizer equalizer = new LongModuleEqualizer();
		equalizer.setTimeout(60);
		equalizer.setPoolConfig(config);
		equalizer.setPlotter(new LoopPlotter());
		// slice 0: master:192.168.1.3:6379 slave:192.168.1.4:6379
		// 192.168.1.5:6379
		equalizer.addSlice(0, "192.168.1.3:6379");
		// slice 1: master:192.168.1.6:6379 slave:192.168.1.7:6379
		// 192.168.1.8:6379
		equalizer.addSlice(1, "192.168.1.2:6379");

		PoolableSlicedRedis redis = new PoolableSlicedRedis(equalizer);
		RedisComand rc = redis.createRedisCommand();
		for (int i = 0; i < 10; i++) {
			rc.set("" + i, "V" + i);
		}
		redis.close();
	}

	 
}
