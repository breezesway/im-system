package com.cgz.message.model;

import com.cgz.im.common.model.message.MessageContent;
import com.cgz.message.dao.ImMessageBodyEntity;
import lombok.Data;

@Data
public class DoStoreP2PMessageDto {

    private MessageContent messageContent;

    private ImMessageBodyEntity imMessageBodyEntity;
}
