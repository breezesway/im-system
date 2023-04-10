package com.cgz.im.tcp.utils;

import com.alibaba.fastjson.JSONObject;
import com.cgz.im.common.constant.Constants;
import com.cgz.im.common.enums.ImConnectStatusEnum;
import com.cgz.im.common.model.UserClientDto;
import com.cgz.im.common.model.UserSession;
import com.cgz.im.tcp.redis.RedisManager;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.AttributeKey;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;

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

    public static void removeUserSession(NioSocketChannel nioSocketChannel){
        //删除session
        String userId = (String) nioSocketChannel.attr(AttributeKey.valueOf(Constants.UserId)).get();
        Integer appId = (Integer) nioSocketChannel.attr(AttributeKey.valueOf(Constants.AppId)).get();
        Integer clientType = (Integer) nioSocketChannel.attr(AttributeKey.valueOf(Constants.ClientType)).get();
        //删除redis
        SessionSocketHolder.remove(appId,userId,clientType);
        RedissonClient redissonClient = RedisManager.getRedissonClient();
        RMap<Object, Object> map = redissonClient.getMap(appId +
                Constants.RedisConstants.UserSessionConstants + userId);
        map.remove(clientType);
        nioSocketChannel.close();
    }

    public static void offlineUserSession(NioSocketChannel nioSocketChannel){
        //删除session
        String userId = (String) nioSocketChannel.attr(AttributeKey.valueOf(Constants.UserId)).get();
        Integer appId = (Integer) nioSocketChannel.attr(AttributeKey.valueOf(Constants.AppId)).get();
        Integer clientType = (Integer) nioSocketChannel.attr(AttributeKey.valueOf(Constants.ClientType)).get();
        SessionSocketHolder.remove(appId,userId,clientType);
        //redis删除
        RedissonClient redissonClient = RedisManager.getRedissonClient();
        RMap<String, String> map = redissonClient.getMap(appId + Constants.RedisConstants.UserSessionConstants + userId);
        String sessionStr = map.get(clientType.toString());
        if(!StringUtils.isBlank(sessionStr)){
            UserSession userSession = JSONObject.parseObject(sessionStr, UserSession.class);
            userSession.setConnectState(ImConnectStatusEnum.OFFLINE_STATUS.getCode());
            map.put(clientType.toString(),JSONObject.toJSONString(userSession));
        }
        nioSocketChannel.close();
    }
}
