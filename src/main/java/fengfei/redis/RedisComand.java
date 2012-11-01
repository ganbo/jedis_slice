package fengfei.redis;

import redis.clients.jedis.BinaryJedisCommands;
import redis.clients.jedis.JedisCommands;

public interface RedisComand extends JedisCommands, BinaryJedisCommands {

}
