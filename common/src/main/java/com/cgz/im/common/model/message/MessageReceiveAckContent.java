package com.cgz.im.common.model.message;

import com.cgz.im.common.model.ClientInfo;
import lombok.Data;

@Data
public class MessageReceiveAckContent extends ClientInfo {

    private Long messageKey;

    private String fromId;

    private String toId;

    private Long messageSequence;
}
