package com.cgz.im.service.message.service;

import com.cgz.im.codec.pack.message.ChatMessageAck;
import com.cgz.im.common.ResponseVO;
import com.cgz.im.common.enums.command.MessageCommand;
import com.cgz.im.common.model.ClientInfo;
import com.cgz.im.common.model.message.MessageContent;
import com.cgz.im.service.message.model.req.SendMessageReq;
import com.cgz.im.service.message.model.resp.SendMessageResp;
import com.cgz.im.service.utils.MessageProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class P2PMessageService {

    private static Logger logger = LoggerFactory.getLogger(P2PMessageService.class);

    @Autowired
    CheckSendMessageService checkSendMessageService;

    @Autowired
    MessageProducer messageProducer;

    @Autowired
    MessageStoreService messageStoreService;

    private final ThreadPoolExecutor threadPoolExecutor;

    {
        AtomicInteger num = new AtomicInteger(0);

        threadPoolExecutor = new ThreadPoolExecutor(8, 8, 60, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(1000), new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                Thread thread = new Thread(r);
                thread.setDaemon(true);
                thread.setName("message-process-thread-" + num.getAndIncrement());
                return thread;
            }
        });
    }


    public void process(MessageContent messageContent){
        String fromId = messageContent.getFromId();
        String toId = messageContent.getToId();
        Integer appId = messageContent.getAppId();
        //前置校验，这个用户是否被禁言、是否被禁用
        //发送方和接收方是否是好友
        /*ResponseVO responseVO = imServerPermissionCheck(fromId, toId, appId);
        if(responseVO.isOk()){*/
            threadPoolExecutor.execute(()->{
                //插入数据
                messageStoreService.storeP2PMessage(messageContent);
                //1.回ack给自己
                ack(messageContent,ResponseVO.successResponse());
                //2.发消息给同步在线端
                syncToSender(messageContent,messageContent);
                //3.发消息给对方在线端
                dispatchMessage(messageContent);
            });
        /*}else{
            //告诉客户端失败了
            ack(messageContent,responseVO);
        }*/
        //回ack
    }

    private void dispatchMessage(MessageContent messageContent){
        messageProducer.sendToUser(messageContent.getToId(),
                MessageCommand.MSG_P2P,
                messageContent,
                messageContent.getAppId());
    }

    private void ack(MessageContent messageContent, ResponseVO responseVO){
        logger.info("msg ack,msgId={},checkResult{}",messageContent.getMessageId(),responseVO.getCode());
        ChatMessageAck chatMessageAck = new ChatMessageAck(messageContent.getMessageId());
        responseVO.setData(chatMessageAck);
        messageProducer.sendToUser(messageContent.getFromId(),
                MessageCommand.MSG_ACK,
                responseVO,
                messageContent);
    }

    private void syncToSender(MessageContent messageContent, ClientInfo clientInfo){
        messageProducer.sendToUserExceptClient(messageContent.getFromId(),
                MessageCommand.MSG_P2P,
                messageContent,
                messageContent);
    }

    public ResponseVO imServerPermissionCheck(String fromId, String toId,
                                               Integer appId){
        ResponseVO responseVO = checkSendMessageService.checkSenderForbidAndMute(fromId, appId);
        if(!responseVO.isOk()){
            return responseVO;
        }
        responseVO = checkSendMessageService.checkFriendShip(fromId, toId, appId);
        return responseVO;
    }

    public SendMessageResp send(SendMessageReq req) {
        SendMessageResp resp = new SendMessageResp();
        MessageContent message = new MessageContent();
        BeanUtils.copyProperties(req,message);
        messageStoreService.storeP2PMessage(message);

        resp.setMessageKey(message.getMessageKey());
        resp.setMessageTime(System.currentTimeMillis());

        syncToSender(message,message);
        dispatchMessage(message);
        return resp;
    }
}
