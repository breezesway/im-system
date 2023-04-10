package com.cgz.im.tcp.handler;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.cgz.im.codec.pack.LoginPack;
import com.cgz.im.codec.proto.Message;
import com.cgz.im.common.constant.Constants;
import com.cgz.im.common.enums.ImConnectStatusEnum;
import com.cgz.im.common.enums.command.SystemCommand;
import com.cgz.im.common.model.UserSession;
import com.cgz.im.tcp.redis.RedisManager;
import com.cgz.im.tcp.utils.SessionSocketHolder;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.AttributeKey;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NettyServerHandler extends SimpleChannelInboundHandler<Message> {

    private final static Logger logger = LoggerFactory.getLogger(NettyServerHandler.class);

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, Message message) throws Exception {

        Integer command = message.getMessageHeader().getCommand();

        if(command == SystemCommand.LOGIN.getCommand()){

            LoginPack loginPack = JSON.parseObject(JSONObject.toJSONString(message.getMessagePack()),new TypeReference<LoginPack>(){}.getType());
            channelHandlerContext.channel().attr(AttributeKey.valueOf("userId")).set(loginPack.getUserId());

            UserSession userSession = new UserSession();
            userSession.setAppId(message.getMessageHeader().getAppId());
            userSession.setClientType(message.getMessageHeader().getClientType());
            userSession.setConnectState(ImConnectStatusEnum.ONLINE_STATUS.getCode());

            //存到Redis
            RedissonClient redissonClient = RedisManager.getRedissonClient();
            RMap<String, String> map = redissonClient.getMap(message.getMessageHeader().getAppId() + Constants.RedisConstants.UserSessionConstants + loginPack.getUserId());
            map.put(message.getMessageHeader().getClientType().toString(),JSONObject.toJSONString(userSession));

            //将channel存起来
            SessionSocketHolder.put(loginPack.getUserId(),(NioSocketChannel) channelHandlerContext.channel());

        }
    }
}
