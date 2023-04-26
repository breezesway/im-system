package com.cgz.im.tcp.utils;

import com.alibaba.fastjson.JSONObject;
import com.cgz.im.codec.pack.user.UserStatusChangeNotifyPack;
import com.cgz.im.codec.proto.MessageHeader;
import com.cgz.im.common.constant.Constants;
import com.cgz.im.common.enums.ImConnectStatusEnum;
import com.cgz.im.common.enums.command.UserEventCommand;
import com.cgz.im.common.model.UserClientDto;
import com.cgz.im.common.model.UserSession;
import com.cgz.im.tcp.mq.MQMessageProducer;
import com.cgz.im.tcp.redis.RedisManager;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.AttributeKey;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 存储channel的工具类
 * 这是Netty的channel，每一个用户(UserClientDto)对应一个channel
 * appId、userId可唯一标识一个用户
 * appId、userId、ClientType、imei可唯一标识一个用户的一台机器
 */
public class SessionSocketHolder {

    private static final Map<UserClientDto, NioSocketChannel> CHANNELS = new ConcurrentHashMap<>();

    public static void put(Integer appId, String userId, Integer clientType, String imei, NioSocketChannel channel) {
        UserClientDto userClientDto = new UserClientDto();
        userClientDto.setAppId(appId);
        userClientDto.setClientType(clientType);
        userClientDto.setImei(imei);
        userClientDto.setUserId(userId);
        CHANNELS.put(userClientDto, channel);
        CHANNELS.forEach((k,v)->{
            System.out.println("put"+k.toString());
            System.out.println("put"+v.toString());
        });
    }

    /**
     * 获取某个用户在某个设备上的channel
     */
    public static NioSocketChannel get(Integer appId, String userId, Integer clientType, String imei) {
        UserClientDto userClientDto = new UserClientDto();
        userClientDto.setAppId(appId);
        userClientDto.setClientType(clientType);
        userClientDto.setImei(imei);
        userClientDto.setUserId(userId);
        System.out.println(userClientDto);
        CHANNELS.forEach((k,v)->{
            if(k.equals(userClientDto)){
                System.out.println("相等");
            }
            System.out.println("get:"+ k);
            System.out.println("get:"+v.toString());
        });
        System.out.println("get:"+CHANNELS.get(userClientDto));
        return CHANNELS.get(userClientDto);
    }

    /**
     * 获取某个用户所有设备上的channel
     */
    public static List<NioSocketChannel> get(Integer appId, String id) {

        Set<UserClientDto> channelInfos = CHANNELS.keySet();
        List<NioSocketChannel> channels = new ArrayList<>();

        channelInfos.forEach(dto -> {
            if (dto.getAppId().equals(appId) && id.equals(dto.getUserId())) {
                channels.add(CHANNELS.get(dto));
            }
        });

        return channels;
    }

    /**
     * 移除某个用户的某个设备的channel
     */
    public static void remove(Integer appId, String userId, Integer clientType, String imei) {
        UserClientDto userClientDto = new UserClientDto();
        userClientDto.setAppId(appId);
        userClientDto.setClientType(clientType);
        userClientDto.setImei(imei);
        userClientDto.setUserId(userId);
        CHANNELS.remove(userClientDto);
    }

    /**
     * 根据channel来删除
     */
    public static void remove(NioSocketChannel nioSocketChannel) {
        CHANNELS.entrySet().stream().filter(e -> e.getValue() == nioSocketChannel)
                .forEach(entry -> CHANNELS.remove(entry.getKey()));
    }

    public static void removeUserSession(NioSocketChannel nioSocketChannel) {
        //删除session
        String userId = (String) nioSocketChannel.attr(AttributeKey.valueOf(Constants.UserId)).get();
        Integer appId = (Integer) nioSocketChannel.attr(AttributeKey.valueOf(Constants.AppId)).get();
        Integer clientType = (Integer) nioSocketChannel.attr(AttributeKey.valueOf(Constants.ClientType)).get();
        String imei = (String) nioSocketChannel.attr(AttributeKey.valueOf(Constants.Imei)).get();
        SessionSocketHolder.remove(appId, userId, clientType, imei);
        //删除redis
        RedissonClient redissonClient = RedisManager.getRedissonClient();
        RMap<Object, Object> map = redissonClient.getMap(appId +
                Constants.RedisConstants.UserSessionConstants + userId);
        map.remove(clientType + ":" + imei);

        MessageHeader msgHeader = new MessageHeader();
        msgHeader.setAppId(appId);
        msgHeader.setImei(imei);
        msgHeader.setClientType(clientType);

        UserStatusChangeNotifyPack notifyPack = new UserStatusChangeNotifyPack();
        notifyPack.setAppId(appId);
        notifyPack.setUserId(userId);
        notifyPack.setStatus(ImConnectStatusEnum.OFFLINE_STATUS.getCode());
        //发送给MQ
        MQMessageProducer.sendMessage(notifyPack,msgHeader, UserEventCommand.USER_ONLINE_STATUS_CHANGE.getCommand());

        nioSocketChannel.close();
    }

    public static void offlineUserSession(NioSocketChannel nioSocketChannel) {
        //删除session
        String userId = (String) nioSocketChannel.attr(AttributeKey.valueOf(Constants.UserId)).get();
        Integer appId = (Integer) nioSocketChannel.attr(AttributeKey.valueOf(Constants.AppId)).get();
        Integer clientType = (Integer) nioSocketChannel.attr(AttributeKey.valueOf(Constants.ClientType)).get();
        String imei = (String) nioSocketChannel.attr(AttributeKey.valueOf(Constants.Imei)).get();
        SessionSocketHolder.remove(appId, userId, clientType, imei);
        //redis删除
        RedissonClient redissonClient = RedisManager.getRedissonClient();
        RMap<String, String> map = redissonClient.getMap(appId +
                Constants.RedisConstants.UserSessionConstants + userId);
        String sessionStr = map.get(clientType.toString() + ":" + imei);
        if (!StringUtils.isBlank(sessionStr)) {
            UserSession userSession = JSONObject.parseObject(sessionStr, UserSession.class);
            userSession.setConnectState(ImConnectStatusEnum.OFFLINE_STATUS.getCode());
            map.put(clientType + ":" + imei, JSONObject.toJSONString(userSession));
        }

        MessageHeader msgHeader = new MessageHeader();
        msgHeader.setAppId(appId);
        msgHeader.setImei(imei);
        msgHeader.setClientType(clientType);

        UserStatusChangeNotifyPack notifyPack = new UserStatusChangeNotifyPack();
        notifyPack.setAppId(appId);
        notifyPack.setUserId(userId);
        notifyPack.setStatus(ImConnectStatusEnum.OFFLINE_STATUS.getCode());
        //发送给MQ
        MQMessageProducer.sendMessage(notifyPack,msgHeader, UserEventCommand.USER_ONLINE_STATUS_CHANGE.getCommand());

        nioSocketChannel.close();
    }
}
