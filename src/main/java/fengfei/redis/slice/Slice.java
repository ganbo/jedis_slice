package fengfei.redis.slice;

import java.util.List;

import org.apache.commons.pool.ObjectPool;
import org.apache.commons.pool.impl.GenericObjectPool;

import fengfei.redis.Plotter;

import redis.clients.jedis.Jedis;

public class Slice {
	public final static int StatusNormal = 1;
	public final static int StatusError = 0;

	protected SliceInfo master;
	protected ObjectPool<Jedis> masterPool;
	protected SliceInfo[] slaves;
	protected ObjectPool<Jedis>[] slavePools;
	protected int slaveSize;
	protected Plotter plotter;
	protected GenericObjectPool.Config config;
	protected int status = StatusNormal;

	public Slice(SliceInfo master, SliceInfo[] slaves,
			Plotter plotter, GenericObjectPool.Config config) {
		super();
		this.master = master;
		this.slaves = slaves;
		this.slaveSize = slaves == null ? 0 : slaves.length;
		this.plotter = plotter;
		this.config = config;
		init();
	}

	public Slice(SliceInfo master, List<SliceInfo> slaves,
			Plotter plotter, GenericObjectPool.Config config) {
		super();
		this.master = master;
		this.slaves = slaves == null ? null : (slaves
				.toArray(new SliceInfo[slaves.size()]));
		this.slaveSize = slaves == null ? 0 : this.slaves.length;
		this.plotter = plotter;
		this.config = config;
		init();
	}

	@SuppressWarnings("unchecked")
	private void init() {
		this.masterPool = new GenericObjectPool<>(new PoolableRedisFactory(
				master.host, master.port, master.timeout * 1000), config);
		if (slaves != null && slaves.length > 0) {
			slavePools = new GenericObjectPool[slaves.length];
			for (int i = 0; i < slaves.length; i++) {
				SliceInfo slave = slaves[i];
				slavePools[i] = new GenericObjectPool<>(
						new PoolableRedisFactory(slave.host, slave.port,
								slave.timeout * 1000), config);
			}
		}
	}

	public ObjectPool<Jedis> getMaster(byte[] key) {
		return masterPool;
	}

	public ObjectPool<Jedis> getAny(byte[] key) {
		int index = plotter.get(key, slaveSize + 1);
		return (slavePools == null || index == slavePools.length) ? masterPool
				: slavePools[index];
	}

	public ObjectPool<Jedis> getNextSlave(byte[] key) {
		if (slaves == null || slaves.length == 0) {
			return masterPool;
		}
		return slavePools[plotter.get(key, slaveSize)];
	}

	public int getStatus() {
		return status;
	}

	public void close() throws Exception {
		masterPool.close();
		if (slavePools == null) {
			return;
		}
		for (ObjectPool<Jedis> objectPool : slavePools) {
			objectPool.close();
		}
	}
}
