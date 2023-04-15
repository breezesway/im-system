package com.cgz.im.codec.pack.group;

import lombok.Data;

/**
 * 群成员禁言通知报文
 **/
@Data
public class GroupMemberSpeakPack {

    private String groupId;

    private String memberId;

    private Long speakDate;

}
