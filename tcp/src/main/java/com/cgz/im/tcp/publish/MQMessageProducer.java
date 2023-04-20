package com.cgz.im.tcp.publish;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.cgz.im.codec.proto.Message;
import com.cgz.im.codec.proto.MessageHeader;
import com.cgz.im.common.constant.Constants;
import com.cgz.im.common.enums.command.CommandType;
import com.cgz.im.tcp.utils.MQFactory;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MQMessageProducer {

    public static void sendMessage(Message message,Integer command){
        Channel channel;
        String channelName = Constants.RabbitConstants.Im2MessageService;
        if (command.toString().startsWith("2")){
            channelName = Constants.RabbitConstants.Im2GroupService;
        }
        try {
            JSONObject o = (JSONObject) JSON.toJSON(message.getMessagePack());
            o.put("command",command);
            o.put("clientType",message.getMessageHeader().getClientType());
            o.put("imei",message.getMessageHeader().getImei());
            o.put("appId",message.getMessageHeader().getAppId());
            channel = MQFactory.getChannel(channelName);
            channel.basicPublish(channelName,
                    "",
                    null,
                    o.toJSONString().getBytes());
        }catch (Exception e){
            log.error("发送消息异常:{}",e.getMessage());
        }
    }

    public static void sendMessage(Object message, MessageHeader header, Integer command){
        Channel channel;
        String com = command.toString();
        String commandSub = com.substring(0, 1);
        CommandType commandType = CommandType.getCommandType(commandSub);
        String channelName = "";
        if(commandType == CommandType.MESSAGE){
            channelName = Constants.RabbitConstants.Im2MessageService;
        }else if(commandType == CommandType.GROUP){
            channelName = Constants.RabbitConstants.Im2GroupService;
        }else if(commandType == CommandType.FRIEND){
            channelName = Constants.RabbitConstants.Im2FriendshipService;
        }else if(commandType == CommandType.USER){
            channelName = Constants.RabbitConstants.Im2UserService;
        }

        try {
            channel = MQFactory.getChannel(channelName);

            JSONObject o = (JSONObject) JSON.toJSON(message);
            o.put("command",command);
            o.put("clientType",header.getClientType());
            o.put("imei",header.getImei());
            o.put("appId",header.getAppId());
            channel.basicPublish(channelName,"",
                    null, o.toJSONString().getBytes());
        }catch (Exception e){
            log.error("发送消息出现异常：{}",e.getMessage());
        }
    }
}
