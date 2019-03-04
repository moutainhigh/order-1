package com.imooc.order.service;

import com.imooc.order.utils.JedisConnectionUtil;
import org.springframework.stereotype.Component;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Transaction;

import java.util.UUID;

/**
 * 分布式锁
 * @author zoomz_lin
 */
@Component
public class DistributedLock {

    /**
     * 获得锁
     * @param lockName 锁的名称
     * @param acquireTimeout 获得锁的超时时间
     * @param lockTimeout 锁本身的过期时间
     * @return
     */
    public String acquireLock(String lockName, long acquireTimeout, long lockTimeout) {
        // 保证释放锁的时候是同一个持有锁的人
        String identifier = UUID.randomUUID().toString();
        String lockKey = "lock:" + lockName;
        int lockExpire = (int)(lockTimeout / 1000);
        Jedis jedis = null;
        try {
            jedis = JedisConnectionUtil.getJedis();
            long end = System.currentTimeMillis() + acquireTimeout;
            while (System.currentTimeMillis() < end) {
                // 设置值成功
                if (jedis.setnx(lockKey, identifier) == 1) {
                    // 设置超时时间
                    jedis.expire(lockKey, lockExpire);
                    return identifier;
                }
                // 检查是否设置了超时时间
                if (jedis.ttl(lockKey) == -1) {
                    // 设置超时时间
                    jedis.expire(lockKey, lockExpire);
                }
                try {
                    // 等待片刻后进行获得锁的重试
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        } finally {
            jedis.close();
        }

        return null;
    }

    // 释放锁
    public boolean releaseLock(String lockName, String identifier) {
        String lockKey = "lock:" + lockName;
        Jedis jedis = null;
        boolean isRelease = false;
        try {
            jedis = JedisConnectionUtil.getJedis();
            // 保证删除key的时候不会被修改，原子性操作
            while (true) {
                jedis.watch(lockKey);
                // 判断是否为同一把锁
                if (identifier.equals(jedis.get(lockKey))) {
                    Transaction transaction = jedis.multi();
                    transaction.del(lockKey);
                    if (transaction.exec().isEmpty()) {
                        continue;
                    }
                    isRelease = true;
                }
                // TODO 异常
                jedis.unwatch();
                break;
            }
        } finally {
            jedis.close();
        }

        return isRelease;
    }

    /**
     * 用lua脚本释放锁
     * 检查redis版本是否大于等于2.6.0
     * 如果版本没问题，打开redis.conf配置文件
     * 检查是否有rename-command EVAL ""这项配置
     * 如果有，注释掉#rename-command EVAL ""，重启redis即可
     * @param lockName
     * @param identifier
     * @return
     */
    public boolean releaseLockWithLua(String lockName, String identifier) {
        Jedis jedis = JedisConnectionUtil.getJedis();
        // lua脚本保证了原子性操作
        String lua = "if redis.call(\"get\", KEYS[1])==ARGV[1] then " +
                "return redis.call(\"del\",KEYS[1]) " +
                "else return 0 end";
        Long ra = (Long) jedis.eval(lua, 1, new String[]{lockName, identifier});
        if (ra.intValue() > 0) {
            return true;
        }
        return false;
    }

}
