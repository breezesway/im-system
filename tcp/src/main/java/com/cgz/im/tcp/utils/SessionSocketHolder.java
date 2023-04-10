package com.cgz.im.tcp.utils;

import com.cgz.im.common.model.UserClientDto;
import io.netty.channel.Channel;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 存储channel的工具类
 */
public class SessionSocketHolder {

    private static final Map<UserClientDto, NioSocketChannel> CHANNELS = new ConcurrentHashMap<>();

    public static void put(Integer appId, String userId, Integer clientType,  NioSocketChannel channel){
        UserClientDto userClientDto = new UserClientDto();
        userClientDto.setAppId(appId);
        userClientDto.setClientType(clientType);
        userClientDto.setUserId(userId);
        CHANNELS.put(userClientDto,channel);
    }

    public static NioSocketChannel get(Integer appId, String userId, Integer clientType){
        UserClientDto userClientDto = new UserClientDto();
        userClientDto.setAppId(appId);
        userClientDto.setClientType(clientType);
        userClientDto.setUserId(userId);
        return CHANNELS.get(userClientDto);
    }

    public static void remove(Integer appId, String userId, Integer clientType){
        UserClientDto userClientDto = new UserClientDto();
        userClientDto.setAppId(appId);
        userClientDto.setClientType(clientType);
        userClientDto.setUserId(userId);
        CHANNELS.remove(userClientDto);
    }

    /**
     * 根据值来删除
     */
    public static void remove(NioSocketChannel nioSocketChannel){
        CHANNELS.entrySet().stream().filter(e->e.getValue() == nioSocketChannel)
                .forEach(entry -> CHANNELS.remove(entry.getKey()));
    }
}
