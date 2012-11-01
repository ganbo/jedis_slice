package fengfei.redis.slice;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;

import org.apache.commons.pool.ObjectPool;
import org.apache.commons.pool.impl.GenericObjectPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import redis.clients.jedis.Jedis;
import fengfei.redis.Equalizer;
import fengfei.redis.RedisComand;

/**
 * <pre>
 * example 1:
 * 		PoolableSlicedRedis redis = new PoolableSlicedRedis(
 * 				"192.168.1.3:6379,192.168.1.4:6379,192.168.1.5:6379 192.168.1.6:6379,192.168.1.7:6379,192.168.1.8:6379", 60000, new HashEqualizer(),
 * 				config);
 * 		RedisComand rc = redis.createRedisCommand();
 * 	    rc.set("key", "value");
 * 		redis.close();
 * 
 * * example 2: 
 * 		Equalizer equalizer = new HashEqualizer();
 * 		equalizer.setTimeout(60);
 * 		equalizer.setPoolConfig(config);
 * 		equalizer.setPlotter(new LoopPlotter());
 * 		//slice 0: master:192.168.1.3:6379 slave:192.168.1.4:6379 192.168.1.5:6379
 * 		equalizer.addSlice(0, "192.168.1.3:6379", "192.168.1.4:6379",
 * 				"192.168.1.5:6379");
 * 		//slice 1: master:192.168.1.6:6379 slave:192.168.1.7:6379 192.168.1.8:6379
 * 		equalizer.addSlice(0, "192.168.1.6:6379", "192.168.1.7:6379",
 * 				"192.168.1.8:6379");
 * 
 * 		PoolableSlicedRedis redis = new PoolableSlicedRedis(equalizer);
 * 		RedisComand rc = redis.createRedisCommand();
 * </pre>
 * 
 * @author
 * 
 */
public class PoolableSlicedRedis {
	final static int ReadWrite = 0;
	final static int ReadOnly = 2;
	final static int WriteOnly = 1;

	private static Logger logger = LoggerFactory
			.getLogger(PoolableSlicedRedis.class);

	private Equalizer equalizer = new HashEqualizer();

	public PoolableSlicedRedis(Equalizer equalizer) {
		this.equalizer = equalizer;
	}

	/**
	 * <pre>
	 * hosts: MasterHost1:port[,Slavehost1-1:port,Slavehost1-2:port...] MasterHost2:port[,Slavehost2-1:port,Slavehost2-2:port...]
	 * 
	 * </pre>
	 * 
	 * @param hosts
	 * @param sliceSize
	 * @param plotter
	 * @param config
	 */
	public PoolableSlicedRedis(String hosts, int timeout, Equalizer equalizer,
			GenericObjectPool.Config config) {
		super();
		this.equalizer = equalizer;

		init(hosts, timeout, config);
	}

	/**
	 * default HashPlotter
	 * 
	 * @param hosts
	 * @param timeout
	 * @param config
	 */
	public PoolableSlicedRedis(String hosts, int timeout,
			GenericObjectPool.Config config) {
		super();
		init(hosts, timeout, config);
	}

	private void init(String hosts, int timeout, GenericObjectPool.Config config) {
		String[] allhosts = hosts.split(" ");

		for (int j = 0; j < allhosts.length; j++) {
			String mshosts = allhosts[j];
			String sliceHosts[] = mshosts.split(",");
			String masterHost = sliceHosts[0];
			String slaveHosts[] = null;
			if (sliceHosts.length > 1) {
				slaveHosts = new String[sliceHosts.length - 1];
				List<String> sliceSlaves = new ArrayList<>();
				for (int i = 1; i < sliceHosts.length; i++) {
					sliceSlaves.add(sliceHosts[i]);
				}
				slaveHosts = sliceSlaves.toArray(slaveHosts);
			}
			equalizer.addSlice(new Long(j), masterHost, slaveHosts);

		}

	}

	public RedisComand createRedisCommand() {
		Class<RedisComand> iface = RedisComand.class;
		RedisComandsHandler handler = new RedisComandsHandler();

		return (RedisComand) Proxy.newProxyInstance(iface.getClassLoader(),
				new Class[] { iface }, handler);
	}

	public RedisComand createRedisCommand(int rw) {
		Class<RedisComand> iface = RedisComand.class;
		RedisComandsHandler handler = new RedisComandsHandler(rw);
		return (RedisComand) Proxy.newProxyInstance(iface.getClassLoader(),
				new Class[] { iface }, handler);
	}

	public void close() {
		Map<Long, Slice> poolables = equalizer.getSliceMap();
		Set<Entry<Long, Slice>> pools = poolables.entrySet();
		for (Entry<Long, Slice> entry : pools) {
			Slice rs = entry.getValue();
			try {
				rs.close();
			} catch (Exception e) {
				e.printStackTrace();
			}

		}
	}

	Random random = new Random(19800202);

	private class RedisComandsHandler implements InvocationHandler {
		int readWrite = ReadWrite;

		public RedisComandsHandler() {

		}

		public RedisComandsHandler(int readWrite) {
			super();
			this.readWrite = readWrite;
		}

		@Override
		public Object invoke(Object proxy, Method method, Object[] args)
				throws Throwable {
			ObjectPool<Jedis> pool = null;
			Jedis jedis = null;
			try {
				byte[] key = null;

				Class<?> argsClass[] = method.getParameterTypes();
				if (args != null && args.length > 0) {
					// argsClass = new Class<?>[args.length];
					// for (int i = 0; i < args.length; i++) {
					// argsClass[i] = args[i].getClass();
					// System.out.println(argsClass[i].isPrimitive());
					// }

					Object obj = args[0];
					if (obj instanceof byte[]) {
						key = (byte[]) obj;
					} else {
						key = obj.toString().getBytes();
					}
				} else {
					// argsClass = new Class<?>[] {};
					key = String.valueOf(random.nextLong()).getBytes();
				}
				Slice redisSlice = equalizer.get(new String(key));
				if (redisSlice == null) {
					throw new Exception("can't find slice.");
				}
				// System.out.println("index: " + index);
				// RedisSlice redisSlice = poolables.get(index);
				switch (readWrite) {
				case ReadWrite:
					pool = redisSlice.getAny(key);
					break;
				case ReadOnly:
					pool = redisSlice.getNextSlave(key);
					break;
				case WriteOnly:
					pool = redisSlice.getMaster(key);
					break;

				default:
					break;
				}

				jedis = pool.borrowObject();
				if (jedis == null) {
					throw new Exception("can't borrow jedis from pool");
				}
				if (!jedis.isConnected()) {
					throw new Exception("redis can't be connected.");
				}
				Method origin = Jedis.class.getMethod(method.getName(),
						argsClass);
				Object obj = origin.invoke(jedis, args);
				return obj;
			} catch (Throwable e) {
				logger.error("Can not operate redis ", e);
				throw e;

			} finally {
				pool.returnObject(jedis);
			}
		}
	}

	public static interface RetryCallback {

		void execute() throws Exception;
	}

}
