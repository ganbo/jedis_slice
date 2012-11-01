package fengfei.redis.slice;

import java.util.Random;

import fengfei.redis.Plotter;

public class RandomPlootter implements Plotter {
	protected Random random = new Random(19791216);



	@Override
	public int get(byte[] key, int size) {
		return Math.abs(random.nextInt() % size);
	}

}
