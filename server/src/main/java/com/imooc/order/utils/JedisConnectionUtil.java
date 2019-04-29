package com.imooc.order.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

/**
 * 获得redis连接
 * @Author zoomz_lin
 */
public class JedisConnectionUtil {
    @Autowired
    private RedisTemplate redisTemplate;
    private static JedisPool pool = null;
    static {
        JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
        jedisPoolConfig.setMaxTotal(100);
        pool = new JedisPool(jedisPoolConfig, "172.20.102.94", 6379);
    }

    public static Jedis getJedis() {
        return pool.getResource();
    }
}
