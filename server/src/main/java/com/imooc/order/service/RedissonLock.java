package com.imooc.order.service;

import org.redisson.Redisson;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;

import java.util.concurrent.TimeUnit;

public class RedissonLock {

    // mvn package打包报错暂时注释
    /*public static void main (String arg[]) {
        Config config = new Config();
        // 单机版的redis
        config.useSingleServer().setAddress("redis://172.20.102.80:6379");
        RedissonClient redissonClient = Redisson.create(config);
        RLock rLock = redissonClient.getLock("updateOrder");
        try {
            rLock.tryLock(100,10, TimeUnit.SECONDS);
            System.out.println("test");
            Thread.sleep(1000);
            rLock.unlock();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            rLock.unlock();
            redissonClient.shutdown();
        }
    }*/
}
