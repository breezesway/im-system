package com.cgz.im.service.message.service;

import com.cgz.im.codec.pack.message.MessageReadedPack;
import com.cgz.im.common.enums.command.Command;
import com.cgz.im.common.enums.command.GroupEventCommand;
import com.cgz.im.common.enums.command.MessageCommand;
import com.cgz.im.common.model.message.MessageReadedContent;
import com.cgz.im.common.model.message.MessageReceiveAckContent;
import com.cgz.im.service.conversation.service.ConversationService;
import com.cgz.im.service.utils.MessageProducer;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MessageSyncService {

    @Autowired
    MessageProducer messageProducer;

    @Autowired
    ConversationService conversationService;

    public void receiveMark(MessageReceiveAckContent messageReceiveAckContent){
        messageProducer.sendToUser(messageReceiveAckContent.getToId(),
                MessageCommand.MSG_RECIVE_ACK,messageReceiveAckContent,
                messageReceiveAckContent.getAppId());
    }

    /**
     * 消息已读，更新会话的seq，通知在线的同步端发送指定command，发送已读回执通知对方
     */
    public void readMark(MessageReadedContent messageContent) {
        conversationService.messageMarkRead(messageContent);
        MessageReadedPack messageReadedPack = new MessageReadedPack();
        BeanUtils.copyProperties(messageContent,messageReadedPack);
        syncToSender(messageReadedPack,messageContent,MessageCommand.MSG_READED_NOTIFY);
        //发送给对方
        messageProducer.sendToUser(messageContent.getToId(),
                MessageCommand.MSG_READED_RECEIPT,messageReadedPack,messageContent.getAppId());
    }

    private void syncToSender(MessageReadedPack pack, MessageReadedContent content, Command command){
        MessageReadedPack messageReadedPack = new MessageReadedPack();
//        BeanUtils.copyProperties(messageReadedContent,messageReadedPack);
        //发送给自己的其他端
        messageProducer.sendToUserExceptClient(pack.getFromId(), command, pack, content);
    }

    public void groupReadMark(MessageReadedContent messageReaded) {
        conversationService.messageMarkRead(messageReaded);
        MessageReadedPack messageReadedPack = new MessageReadedPack();
        BeanUtils.copyProperties(messageReaded,messageReadedPack);
        syncToSender(messageReadedPack,messageReaded, GroupEventCommand.MSG_GROUP_READED_NOTIFY
        );
        if(!messageReaded.getFromId().equals(messageReaded.getToId())){
            messageProducer.sendToUser(messageReadedPack.getToId(),GroupEventCommand.MSG_GROUP_READED_RECEIPT
                    ,messageReaded,messageReaded.getAppId());
        }
    }
}
