package com.cgz.im.service.friendship.service;

import com.cgz.im.common.ResponseVO;
import com.cgz.im.service.friendship.model.req.AddFriendShipGroupMemberReq;
import com.cgz.im.service.friendship.model.req.DeleteFriendShipGroupMemberReq;

/**
 * 好友分组成员
 */
public interface ImFriendShipGroupMemberService {

    ResponseVO addGroupMember(AddFriendShipGroupMemberReq req);

    ResponseVO delGroupMember(DeleteFriendShipGroupMemberReq req);

    int doAddGroupMember(Long groupId, String toId);

    int clearGroupMember(Long groupId);
}
