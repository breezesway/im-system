package com.cgz.im.codec.pack.friendship;

import lombok.Data;


/**
 * 修改好友通知报文
 **/
@Data
public class UpdateFriendPack {

    public String fromId;

    private String toId;

    private String remark;

    private Long sequence;
}
