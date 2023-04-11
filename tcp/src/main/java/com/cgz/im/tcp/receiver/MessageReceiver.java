package com.cgz.im.tcp.receiver;

import com.alibaba.fastjson.JSONObject;
import com.cgz.im.codec.proto.MessagePack;
import com.cgz.im.common.constant.Constants;
import com.cgz.im.tcp.utils.MQFactory;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

@Slf4j
public class MessageReceiver {

    private static void startReceiveMessage() {
        try {
            Channel channel = MQFactory.getChannel(Constants.RabbitConstants.MessageService2Im);
            channel.queueDeclare(Constants.RabbitConstants.MessageService2Im, true, false, false, null);
            channel.queueBind(Constants.RabbitConstants.MessageService2Im,
                    Constants.RabbitConstants.MessageService2Im,
                    null);
            channel.basicConsume(Constants.RabbitConstants.MessageService2Im,
                    false,
                    new DefaultConsumer(channel) {
                        @Override
                        public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                            //TODO 处理消息服务发来的消息
                            String msgStr = new String(body);
                            System.out.println(msgStr);
                            log.info(msgStr);
                            /*try {
                                String msgStr = new String(body);
                                log.info(msgStr);
                                MessagePack messagePack =
                                        JSONObject.parseObject(msgStr, MessagePack.class);
                                BaseProcess messageProcess = ProcessFactory
                                        .getMessageProcess(messagePack.getCommand());
                                messageProcess.process(messagePack);

                                channel.basicAck(envelope.getDeliveryTag(),false);

                            }catch (Exception e){
                                e.printStackTrace();
                                channel.basicNack(envelope.getDeliveryTag(),false,false);
                            }*/
                        }
                    }
            );
        } catch (Exception e) {

        }
    }

    public static void init(){
        startReceiveMessage();
    }
}
