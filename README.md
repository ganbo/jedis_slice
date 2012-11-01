#Jedis Slice for jedis java client#
**Jedis Slice** is a slice framework for jedis.
**jedis** is a java client for Redis, as follow [jedis](https://github.com/xetorthio/jedis).

#Usage#
**example 1:**

 	PoolableSlicedRedis redis = new PoolableSlicedRedis("192.168.1.3:6379 192.168.1.4:6380", 60000,new HashEqualizer(), config);
	//PoolableSlicedRedis redis = new PoolableSlicedRedis("192.168.1.3:6379 192.168.1.4:6380", 60000,new LongModuleEqualizer(), config);
	RedisComand rc = redis.createRedisCommand();

	for (int i = 0; i < 10; i++) {
		rc.set("K" + i, "V" + i);
	}
	redis.close();

**example 2:**

		//Equalizer equalizer = new HashEqualizer();
		Equalizer equalizer = new LongModuleEqualizer();
		equalizer.setTimeout(60);
		equalizer.setPoolConfig(config);
		equalizer.setPlotter(new LoopPlotter());

		//slice 0: master:192.168.1.3:6379 slave:192.168.1.4:6379 ,192.168.1.5:6379
		equalizer.addSlice(0, "192.168.1.3:6379");
		//slice 1: master:192.168.1.6:6379 slave:192.168.1.7:6379,192.168.1.8:6379
		equalizer.addSlice(1, "192.168.1.2:6379");

		PoolableSlicedRedis redis = new PoolableSlicedRedis(equalizer);
		RedisComand rc = redis.createRedisCommand();
		for (int i = 0; i < 10; i++) {
			rc.set("" + i, "V" + i);
		}
		redis.close();