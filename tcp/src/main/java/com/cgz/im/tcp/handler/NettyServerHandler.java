package com.cgz.im.tcp.handler;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.cgz.im.codec.pack.LoginPack;
import com.cgz.im.codec.proto.Message;
import com.cgz.im.codec.proto.MessageHeader;
import com.cgz.im.common.constant.Constants;
import com.cgz.im.common.enums.ImConnectStatusEnum;
import com.cgz.im.common.enums.command.SystemCommand;
import com.cgz.im.common.model.UserClientDto;
import com.cgz.im.common.model.UserSession;
import com.cgz.im.tcp.publish.MQMessageProducer;
import com.cgz.im.tcp.redis.RedisManager;
import com.cgz.im.tcp.utils.SessionSocketHolder;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.AttributeKey;
import org.redisson.api.RMap;
import org.redisson.api.RTopic;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;

public class NettyServerHandler extends SimpleChannelInboundHandler<Message> {

    private final static Logger logger = LoggerFactory.getLogger(NettyServerHandler.class);

    private Integer brokerId;

    public NettyServerHandler(Integer brokerId) {
        this.brokerId = brokerId;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Message msg) throws Exception {

        Integer command = msg.getMessageHeader().getCommand();

        if(command == SystemCommand.LOGIN.getCommand()){ //登录

            MessageHeader msgHeader = msg.getMessageHeader();
            LoginPack loginPack = JSON.parseObject(JSONObject.toJSONString(msg.getMessagePack()),new TypeReference<LoginPack>(){}.getType());

            ctx.channel().attr(AttributeKey.valueOf(Constants.UserId)).set(loginPack.getUserId());
            ctx.channel().attr(AttributeKey.valueOf(Constants.AppId)).set(msgHeader.getAppId());
            ctx.channel().attr(AttributeKey.valueOf(Constants.ClientType)).set(msgHeader.getClientType());
            ctx.channel().attr(AttributeKey.valueOf(Constants.Imei)).set(msgHeader.getImei());

            UserSession userSession = new UserSession();
            userSession.setAppId(msgHeader.getAppId());
            userSession.setClientType(msgHeader.getClientType());
            userSession.setUserId(loginPack.getUserId());
            userSession.setConnectState(ImConnectStatusEnum.ONLINE_STATUS.getCode());
            userSession.setBrokerId(brokerId);
            userSession.setImei(msgHeader.getImei());
            try {
                InetAddress localHost = InetAddress.getLocalHost();
                userSession.setBrokerHost(localHost.getHostAddress());
            }catch (Exception e){
                e.printStackTrace();
            }

            //存到Redis
            RedissonClient redissonClient = RedisManager.getRedissonClient();
            RMap<String, String> map = redissonClient.getMap(msgHeader.getAppId() + Constants.RedisConstants.UserSessionConstants + loginPack.getUserId());
            map.put(msgHeader.getClientType()+":"+ msgHeader.getImei(),JSONObject.toJSONString(userSession));

            //将channel存起来
            SessionSocketHolder.put(msgHeader.getAppId(),
                    loginPack.getUserId(),
                    msgHeader.getClientType(),
                    msgHeader.getImei(),
                    (NioSocketChannel) ctx.channel());

            UserClientDto dto = new UserClientDto();
            dto.setImei(msgHeader.getImei());
            dto.setUserId(loginPack.getUserId());
            dto.setClientType(msgHeader.getClientType());
            dto.setAppId(msgHeader.getAppId());

            //这里采用Redis的发布订阅，因为redis的发布订阅可发送给所有端
            RTopic topic = redissonClient.getTopic(Constants.RedisConstants.UserLoginChannel);
            topic.publish(JSONObject.toJSONString(dto));

        }else if(command == SystemCommand.LOGOUT.getCommand()){ //退出
            SessionSocketHolder.removeUserSession((NioSocketChannel) ctx.channel());
        }else if (command == SystemCommand.PING.getCommand()){ //心跳
            ctx.channel().attr(AttributeKey.valueOf(Constants.ReadTime)).set(System.currentTimeMillis());
        }else {
            MQMessageProducer.sendMessage(msg,command);
        }
    }
}
