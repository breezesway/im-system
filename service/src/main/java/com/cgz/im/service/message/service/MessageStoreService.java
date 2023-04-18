package com.cgz.im.service.message.service;

import com.alibaba.fastjson.JSONObject;
import com.cgz.im.common.config.AppConfig;
import com.cgz.im.common.constant.Constants;
import com.cgz.im.common.enums.ConversationTypeEnum;
import com.cgz.im.common.enums.DelFlagEnum;
import com.cgz.im.common.model.message.*;
import com.cgz.im.service.conversation.service.ConversationService;
import com.cgz.im.service.group.dao.ImGroupMessageHistoryEntity;
import com.cgz.im.service.group.dao.mapper.ImGroupMessageHistoryMapper;
import com.cgz.im.service.message.dao.ImMessageBodyEntity;
import com.cgz.im.service.message.dao.ImMessageHistoryEntity;
import com.cgz.im.service.message.dao.mapper.ImMessageBodyMapper;
import com.cgz.im.service.message.dao.mapper.ImMessageHistoryMapper;
import com.cgz.im.service.utils.SnowflakeIdWorker;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class MessageStoreService {

    @Autowired
    ImMessageHistoryMapper imMessageHistoryMapper;

    @Autowired
    ImMessageBodyMapper imMessageBodyMapper;

    @Autowired
    SnowflakeIdWorker snowflakeIdWorker;

    @Autowired
    ImGroupMessageHistoryMapper imGroupMessageHistoryMapper;

    @Autowired
    RabbitTemplate rabbitTemplate;

    @Autowired
    StringRedisTemplate stringRedisTemplate;

    @Autowired
    ConversationService conversationService;

    @Autowired
    AppConfig appConfig;

    @Transactional
    public void storeP2PMessage(MessageContent messageContent){
        //messageContent转化成MessageBody
        ImMessageBody imMessageBody = extractMessageBody(messageContent);
        /*//插入MessageBody
        imMessageBodyMapper.insert(imMessageBodyEntity);
        //转化成MessageHistory
        List<ImMessageHistoryEntity> imMessageHistoryEntities = extractToP2PMessageHistory(messageContent, imMessageBodyEntity);
        //批量插入
        imMessageHistoryMapper.insertBatchSomeColumn(imMessageHistoryEntities);
        messageContent.setMessageKey(imMessageBodyEntity.getMessageKey());*/
        DoStoreP2PMessageDto dto = new DoStoreP2PMessageDto();
        dto.setMessageContent(messageContent);
        dto.setImMessageBody(imMessageBody);
        messageContent.setMessageKey(imMessageBody.getMessageKey());
        //发送MQ消息
        rabbitTemplate.convertAndSend(Constants.RabbitConstants.StoreP2PMessage,
                "",
                JSONObject.toJSONString(dto));
    }

    public ImMessageBody extractMessageBody(MessageContent messageContent){
        ImMessageBody messageBody = new ImMessageBody();
        messageBody.setAppId(messageContent.getAppId());
        messageBody.setMessageKey(snowflakeIdWorker.nextId());
        messageBody.setCreateTime(System.currentTimeMillis());
        messageBody.setSecurityKey("");
        messageBody.setExtra(messageContent.getExtra());
        messageBody.setDelFlag(DelFlagEnum.NORMAL.getCode());
        messageBody.setMessageTime(messageContent.getMessageTime());
        messageBody.setMessageBody(messageContent.getMessageBody());
        return messageBody;
    }

    public List<ImMessageHistoryEntity> extractToP2PMessageHistory(MessageContent messageContent,
                                                                   ImMessageBodyEntity imMessageBodyEntity){
        List<ImMessageHistoryEntity> list = new ArrayList<>();
        ImMessageHistoryEntity fromHistory = new ImMessageHistoryEntity();
        BeanUtils.copyProperties(messageContent,fromHistory);
        fromHistory.setOwnerId(messageContent.getFromId());
        fromHistory.setMessageKey(imMessageBodyEntity.getMessageKey());
        fromHistory.setCreateTime(System.currentTimeMillis());

        ImMessageHistoryEntity toHistory = new ImMessageHistoryEntity();
        BeanUtils.copyProperties(messageContent,toHistory);
        toHistory.setOwnerId(messageContent.getToId());
        toHistory.setMessageKey(imMessageBodyEntity.getMessageKey());
        toHistory.setCreateTime(System.currentTimeMillis());

        list.add(fromHistory);
        list.add(toHistory);
        return list;
    }

    @Transactional
    public void storeGroupMessage(GroupChatMessageContent messageContent){
        /*ImMessageBodyEntity imMessageBodyEntity = extractMessageBody(messageContent);
        imMessageBodyMapper.insert(imMessageBodyEntity);
        ImGroupMessageHistoryEntity imGroupMessageHistoryEntity = extractToGroupMessageHistory(messageContent, imMessageBodyEntity);
        imGroupMessageHistoryMapper.insert(imGroupMessageHistoryEntity);
        messageContent.setMessageKey(imMessageBodyEntity.getMessageKey());*/
        ImMessageBody imMessageBody = extractMessageBody(messageContent);
        DoStoreGroupMessageDto dto = new DoStoreGroupMessageDto();
        dto.setMessageBody(imMessageBody);
        dto.setGroupChatMessageContent(messageContent);
        rabbitTemplate.convertAndSend(Constants.RabbitConstants.StoreGroupMessage,
                "",
                JSONObject.toJSONString(dto));
        messageContent.setMessageKey(imMessageBody.getMessageKey());
    }

    private ImGroupMessageHistoryEntity extractToGroupMessageHistory(GroupChatMessageContent messageContent ,
                                                                     ImMessageBodyEntity messageBodyEntity){
        ImGroupMessageHistoryEntity result = new ImGroupMessageHistoryEntity();
        BeanUtils.copyProperties(messageContent,result);
        result.setGroupId(messageContent.getGroupId());
        result.setMessageKey(messageBodyEntity.getMessageKey());
        result.setCreateTime(System.currentTimeMillis());
        return result;
    }

    public void setMessageFromMessageIdCache(Integer appId, String messageId, Object messageContent){
        //appid:cache:messageId
        String key = appId+":"+Constants.RedisConstants.cacheMessage+":"+messageId;
        stringRedisTemplate.opsForValue().set(key,JSONObject.toJSONString(messageContent),300, TimeUnit.SECONDS);
    }

    public <T> T getMessageFromMessageIdCache(Integer appId,String messageId,Class<T> clazz){
        //appid:cache:messageId
        String key = appId+":"+Constants.RedisConstants.cacheMessage+":"+messageId;
        String msg = stringRedisTemplate.opsForValue().get(key);
        if(StringUtils.isBlank(msg)){
            return null;
        }
        return JSONObject.parseObject(msg,clazz);
    }

    /**
     * 存储单人离线消息
     */
    public void storeOfflineMessage(OfflineMessageContent offlineMessage){

        //找到fromId队列
        //找到toId的队列
        String fromKey = offlineMessage.getAppId()+":"+Constants.RedisConstants.OfflineMessage+":"+offlineMessage.getFromId();
        String toKey = offlineMessage.getAppId()+":"+Constants.RedisConstants.OfflineMessage+":"+offlineMessage.getToId();
        ZSetOperations<String, String> operations = stringRedisTemplate.opsForZSet();
        //判断队列中的数据是否超过设定值
        if(operations.zCard(fromKey)>appConfig.getOfflineMessageCount()){
            operations.removeRange(fromKey,0,0);
        }
        offlineMessage.setConversationId(conversationService.convertConversationId(
                ConversationTypeEnum.P2P.getCode(), offlineMessage.getFromId(), offlineMessage.getToId()
        ));
        //插入数据 根据MessageKey 作为分值
        operations.add(fromKey,JSONObject.toJSONString(offlineMessage), offlineMessage.getMessageKey());

        if(operations.zCard(toKey)>appConfig.getOfflineMessageCount()){
            operations.removeRange(toKey,0,0);
        }
        offlineMessage.setConversationId(conversationService.convertConversationId(
                ConversationTypeEnum.P2P.getCode(), offlineMessage.getToId(), offlineMessage.getFromId()
        ));
        operations.add(toKey,JSONObject.toJSONString(offlineMessage), offlineMessage.getMessageKey());

    }

    /**
     * 存储群聊离线消息
     */
    public void storeGroupOfflineMessage(OfflineMessageContent offlineMessage,List<String> memberIds){

        ZSetOperations<String, String> operations = stringRedisTemplate.opsForZSet();

        for(String memberId:memberIds){
            String toKey = offlineMessage.getAppId()+":"+
                    Constants.RedisConstants.OfflineMessage+":"+memberId;
            offlineMessage.setConversationId(conversationService.convertConversationId(
                    ConversationTypeEnum.GROUP.getCode(), offlineMessage.getFromId(), offlineMessage.getToId()
            ));
            if(operations.zCard(toKey)>appConfig.getOfflineMessageCount()){
                operations.removeRange(toKey,0,0);
            }
            operations.add(toKey,JSONObject.toJSONString(offlineMessage), offlineMessage.getMessageKey());

        }
    }
}
