package com.imooc.order.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * Redis分布式锁
 * @Author: zoomz_lin
 */
@Component
@Slf4j
public class RedisLock {
    @Autowired
    private StringRedisTemplate redisTemplate;

    /**
     * 加锁
     * @param key
     * @param value 当前时间+超时时间
     * @return
     */
    public boolean lock(String key, String value) {
        if (redisTemplate.opsForValue().setIfAbsent(key, value)) {
            return true;
        }

        // 考虑到下单减库存的时候可能出现抛异常或者网络等原因出错了，造成锁的状态一直是锁住的状态，其他用户进来拿不到锁就不进行后续的流程操作
        String currentValue = redisTemplate.opsForValue().get(key);
        // 如果锁已过期
        if (!StringUtils.isEmpty(currentValue) && Long.parseLong(currentValue) < System.currentTimeMillis()) {
            // getAndSet顾名思义就是从redis中取出数据放到oldValue中并且将新的value的值放到对应的key中
            String oldValue = redisTemplate.opsForValue().getAndSet(key, value);
            if (!StringUtils.isEmpty(oldValue) && oldValue.equals(oldValue)) {
                return true;
            }
        }

        return false;
    }

    /**
     * 释放锁
     * @param key
     * @param value
     */
    public void unlock(String key, String value) {
        try {
            String currentValue = redisTemplate.opsForValue().get(key);
            if (!StringUtils.isEmpty(currentValue) && currentValue.equals(value)) {
                redisTemplate.opsForValue().getOperations().delete(key);
            }
        } catch (Exception e) {
            log.info("【redis分布式锁】释放锁异常，{}", e);
        }
    }

}
