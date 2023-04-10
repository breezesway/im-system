package com.cgz.im.service.friendship.service;

import com.cgz.im.common.ResponseVO;
import com.cgz.im.service.friendship.dao.ImFriendShipGroupEntity;
import com.cgz.im.service.friendship.model.req.AddFriendShipGroupReq;
import com.cgz.im.service.friendship.model.req.DeleteFriendShipGroupReq;

/**
 * 好友分组
 */
public interface ImFriendShipGroupService {

    ResponseVO addGroup(AddFriendShipGroupReq req);

    ResponseVO deleteGroup(DeleteFriendShipGroupReq req);

    ResponseVO<ImFriendShipGroupEntity> getGroup(String fromId, String groupName, Integer appId);

}
