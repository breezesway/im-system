package com.cgz.im.tcp.publish;

import com.alibaba.fastjson.JSONObject;
import com.cgz.im.tcp.utils.MQFactory;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;

/**
 * 暂时未用到
 */
@Slf4j
public class MQMessageProducer {

    public static void sendMessage(Object message){
        //TODO
        Channel channel = null;
        String channelName = "";
        try {
            MQFactory.getChannel(channelName);
            channel.basicPublish(channelName,"",null, JSONObject.toJSONString(message).getBytes());
        }catch (Exception e){
            log.error("发送消息异常:{}",e.getMessage());
        }
    }
}
