package com.cgz.im.tcp.redis;

import com.cgz.im.codec.config.BootstrapConfig;
import com.cgz.im.tcp.receiver.UserLoginMessageListener;
import org.redisson.api.RedissonClient;

public class RedisManager {

    private static RedissonClient redissonClient;

    private static Integer loginModel;

    public static void init(BootstrapConfig config){
        loginModel = config.getLim().getLoginModel();

        SingleClientStrategy singleClientStrategy = new SingleClientStrategy();
        redissonClient = singleClientStrategy.getRedissonClient(config.getLim().getRedis());

        UserLoginMessageListener userLoginMessageListener = new UserLoginMessageListener(loginModel);
        userLoginMessageListener.listenerUserLogin();

    }

    public static RedissonClient getRedissonClient(){
        return redissonClient;
    }
}
