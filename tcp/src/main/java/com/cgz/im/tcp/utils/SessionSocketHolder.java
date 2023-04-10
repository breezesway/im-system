package com.cgz.im.tcp.utils;

import io.netty.channel.socket.nio.NioSocketChannel;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 存储channel的工具类
 */
public class SessionSocketHolder {

    private static final Map<String, NioSocketChannel> CHANNELS = new ConcurrentHashMap<>();

    public static void put(String userId, NioSocketChannel channel){
        CHANNELS.put(userId,channel);
    }

    public static void get(String userId){
        CHANNELS.get(userId);
    }
}
