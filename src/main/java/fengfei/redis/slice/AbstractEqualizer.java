package fengfei.redis.slice;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.pool.impl.GenericObjectPool;
import org.apache.commons.pool.impl.GenericObjectPool.Config;

import fengfei.redis.Equalizer;
import fengfei.redis.Plotter;

/**
 * key -> hash % size->slice
 * 
 * 
 */
public abstract class AbstractEqualizer implements Equalizer {
	protected int timeout = 60;// seconds
	protected Config config;
	protected Plotter plotter;

	public AbstractEqualizer() {
		config = new GenericObjectPool.Config();
		config.maxActive = 10;
		config.maxIdle = 10;
		config.minIdle = 2;
		config.maxWait = 60000;
		config.testOnBorrow = true;
		config.whenExhaustedAction = GenericObjectPool.WHEN_EXHAUSTED_FAIL;
		plotter = new HashPlotter();
	}

	public AbstractEqualizer(int timeout, Config config, Plotter plotter) {
		super();
		this.timeout = timeout;
		this.config = config;
		this.plotter = plotter;
	}

	@Override
	public void addSlice(long id, String masterHost, String... slaveHosts) {
		String mhp[] = masterHost.split(":");
		SliceInfo master = new SliceInfo(mhp[0],
				Integer.parseInt(mhp[1]), timeout);
		List<SliceInfo> slaves = new ArrayList<>();
		if (slaveHosts != null && slaveHosts.length > 0) {

			for (String shost : slaveHosts) {
				String shp[] = shost.split(":");
				SliceInfo slave = new SliceInfo(shp[0],
						Integer.parseInt(shp[1]), timeout);
				slaves.add(slave);
			}

		}
		Slice redisSlice = new Slice(master, slaves, plotter, config);
		getSliceMap().put(id, redisSlice);

	}

	@Override
	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}

	@Override
	public void setPoolConfig(Config config) {
		this.config = config;

	}

	@Override
	public void setPlotter(Plotter plotter) {
		this.plotter = plotter;
	}
}
