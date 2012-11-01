package fengfei.redis.slice;

import redis.clients.util.Hashing;
import fengfei.redis.Plotter;

public class HashPlotter implements Plotter {
	protected Hashing hashed = Hashing.MURMUR_HASH;

	@Override
	public int get(byte[] key, int size) {
		return Math.abs((Long.valueOf(hashed.hash(key) % size).intValue()));
	}
}
