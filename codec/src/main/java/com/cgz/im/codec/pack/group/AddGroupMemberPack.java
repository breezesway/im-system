package com.cgz.im.codec.pack.group;

import lombok.Data;

import java.util.List;

/**
 * 群内添加群成员通知报文
 **/
@Data
public class AddGroupMemberPack {

    private String groupId;

    private List<String> members;

}
