package com.cgz.message.model;

import com.cgz.im.common.model.message.GroupChatMessageContent;
import com.cgz.im.common.model.message.MessageContent;
import com.cgz.message.dao.ImMessageBodyEntity;
import lombok.Data;

@Data
public class DoStoreGroupMessageDto {

    private GroupChatMessageContent groupChatMessageContent;

    private ImMessageBodyEntity imMessageBodyEntity;
}
