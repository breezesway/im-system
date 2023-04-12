package com.cgz.im.tcp.receiver;

import com.cgz.im.common.constant.Constants;
import com.cgz.im.tcp.utils.MQFactory;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;

/**
 * 暂时未用到
 */
@Slf4j
public class MessageReceiver {

    private static String brokerId;

    private static void startReceiveMessage() {
        try {
            Channel channel = MQFactory.getChannel(Constants.RabbitConstants.MessageService2Im+brokerId);
            channel.queueDeclare(Constants.RabbitConstants.MessageService2Im+brokerId, true, false, false, null);
            channel.queueBind(Constants.RabbitConstants.MessageService2Im+brokerId,
                    Constants.RabbitConstants.MessageService2Im,
                    brokerId);
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

    public static void init(String brokerId){
        if(StringUtils.isBlank(MessageReceiver.brokerId)){
            MessageReceiver.brokerId = brokerId;
        }
        startReceiveMessage();
    }
}
