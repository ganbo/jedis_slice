package fengfei.redis.slice;


public class SliceInfo {

	protected final String host;
	protected final int port;
	protected final int timeout;
	protected boolean isMaster = true;

	public SliceInfo(String host, int port, int timeout) {
		super();
		this.host = host;
		this.port = port;
		this.timeout = timeout;

	}

	public SliceInfo(String host, int port, int timeout, boolean isMaster) {
		this(host, port, timeout);
		this.isMaster = isMaster;
	}

	public String getHost() {
		return host;
	}

	public int getPort() {
		return port;
	}

	@Override
	public String toString() {
		return "RedisSliceInfo [host=" + host + ", port=" + port + ", timeout="
				+ timeout + ", isMaster=" + isMaster + "]";
	}

}
