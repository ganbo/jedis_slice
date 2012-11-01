package fengfei.redis.slice;

import java.rmi.ConnectException;

import org.apache.commons.pool.PoolableObjectFactory;

import redis.clients.jedis.Jedis;

public class PoolableRedisFactory implements PoolableObjectFactory<Jedis> {

	private String host;
	private int port;
	private int timeout = 5000;
	private String password;

	public PoolableRedisFactory(final String host, final int port,
			final int timeout, final String password) {
		this(host, port, timeout);
		this.password = password;
	}

	public PoolableRedisFactory(final String host, final int port,
			final int timeout) {
		super();
		this.host = host;
		this.port = port;
		this.timeout = timeout;
		password = null;
	}

	public Jedis makeObject() throws Exception {
		Jedis jedis;
		try {
			jedis = new Jedis(this.host, this.port, this.timeout);

			jedis.connect();
			if (null != this.password) {
				jedis.auth(this.password);
			}
		} catch (Exception e) {
			throw new ConnectException("Can't connect host:" + host + ":"
					+ port, e);
		}

		return jedis;
	}

	public void destroyObject(final Jedis obj) throws Exception {
		if (obj instanceof Jedis) {
			final Jedis jedis = (Jedis) obj;
			if (jedis.isConnected()) {
				try {
					try {
						jedis.quit();
					} catch (Exception e) {
					}
					jedis.disconnect();
				} catch (Exception e) {

				}
			}
		}
	}

	public boolean validateObject(final Jedis obj) {
		if (obj instanceof Jedis) {
			final Jedis jedis = (Jedis) obj;
			try {
				return jedis.isConnected() && jedis.ping().equals("PONG");
			} catch (final Exception e) {
				return false;
			}
		} else {
			return false;
		}
	}

	@Override
	public void activateObject(Jedis obj) throws Exception {
		if (obj instanceof Jedis) {
			final Jedis jedis = (Jedis) obj;
			try {
				jedis.connect();
			} catch (final Exception e) {
				e.printStackTrace();
			}
		}

	}

	@Override
	public void passivateObject(Jedis obj) throws Exception {

	}
}
