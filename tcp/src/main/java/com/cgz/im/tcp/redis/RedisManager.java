package com.cgz.im.tcp.redis;

import com.cgz.im.codec.config.BootstrapConfig;
import org.redisson.api.RedissonClient;

public class RedisManager {

    private static RedissonClient redissonClient;

    public static void init(BootstrapConfig config){
        SingleClientStrategy singleClientStrategy = new SingleClientStrategy();
        redissonClient = singleClientStrategy.getRedissonClient(config.getLim().getRedis());
    }

    public static RedissonClient getRedissonClient(){
        return redissonClient;
    }
}
