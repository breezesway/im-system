package com.cgz.im.codec.pack.message;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 撤回消息通知报文
 **/
@Data
@NoArgsConstructor
public class RecallMessageNotifyPack {

    private String fromId;

    private String toId;

    private Long messageKey;

    private Long messageSequence;
}
