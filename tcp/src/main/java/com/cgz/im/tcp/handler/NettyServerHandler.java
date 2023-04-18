package com.cgz.im.tcp.handler;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.cgz.im.codec.pack.LoginPack;
import com.cgz.im.codec.pack.message.ChatMessageAck;
import com.cgz.im.codec.proto.Message;
import com.cgz.im.codec.proto.MessageHeader;
import com.cgz.im.codec.proto.MessagePack;
import com.cgz.im.common.ResponseVO;
import com.cgz.im.common.constant.Constants;
import com.cgz.im.common.enums.ImConnectStatusEnum;
import com.cgz.im.common.enums.command.GroupEventCommand;
import com.cgz.im.common.enums.command.MessageCommand;
import com.cgz.im.common.enums.command.SystemCommand;
import com.cgz.im.common.model.UserClientDto;
import com.cgz.im.common.model.UserSession;
import com.cgz.im.common.model.message.CheckSendMessageReq;
import com.cgz.im.tcp.feign.FeignMessageService;
import com.cgz.im.tcp.publish.MQMessageProducer;
import com.cgz.im.tcp.redis.RedisManager;
import com.cgz.im.tcp.utils.SessionSocketHolder;
import feign.Feign;
import feign.Request;
import feign.jackson.JacksonDecoder;
import feign.jackson.JacksonEncoder;
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

    private String logicUrl;

    private FeignMessageService feignMessageService;

    public NettyServerHandler(Integer brokerId,String logicUrl) {
        this.brokerId = brokerId;
        feignMessageService = Feign.builder()
                .encoder(new JacksonEncoder())
                .decoder(new JacksonDecoder())
                .options(new Request.Options(1000,3500))
                .target(FeignMessageService.class,logicUrl);
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
        }else if(command == MessageCommand.MSG_P2P.getCommand() ||
        command == GroupEventCommand.MSG_GROUP.getCommand()){
            try {
                String toId = "";
                CheckSendMessageReq req = new CheckSendMessageReq();
                req.setAppId(msg.getMessageHeader().getAppId());
                req.setCommand(msg.getMessageHeader().getCommand());
                JSONObject jsonObject = JSON.parseObject(JSONObject.toJSONString(msg.getMessagePack()));
                String fromId = jsonObject.getString("fromId");
                if(command == MessageCommand.MSG_P2P.getCommand()){
                    toId = jsonObject.getString("toId");
                }else {
                    toId = jsonObject.getString("groupId");
                }
                req.setFromId(fromId);
                req.setToId(toId);
                //1.调用校验消息发送方的接口
                ResponseVO responseVO = feignMessageService.checkSendMessage(req);
                if(responseVO.isOk()){
                    MQMessageProducer.sendMessage(msg,command);
                }else{
                    Integer ackCommand;
                    if(command == MessageCommand.MSG_P2P.getCommand()){
                        ackCommand = MessageCommand.MSG_ACK.getCommand();
                    }else {
                        ackCommand = GroupEventCommand.GROUP_MSG_ACK.getCommand();
                    }
                    ChatMessageAck chatMessageAck = new ChatMessageAck(jsonObject.getString("messageId"));
                    MessagePack<ResponseVO> ack = new MessagePack<>();
                    ack.setData(responseVO);
                    ack.setCommand(ackCommand);
                    ctx.channel().writeAndFlush(ack);
                }
            }catch (Exception e){

            }

            //成功则投递到Mq,失败则直接ack
        }else {
            MQMessageProducer.sendMessage(msg,command);
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        SessionSocketHolder.offlineUserSession((NioSocketChannel) ctx.channel());
        ctx.close();
    }
}
