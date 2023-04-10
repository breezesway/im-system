package com.cgz.im.service.friendship.service;

import com.cgz.im.common.ResponseVO;
import com.cgz.im.common.model.RequestBase;
import com.cgz.im.service.friendship.model.req.*;

/**
 * 朋友关系，包括好友和黑名单
 */
public interface ImFriendService {

    ResponseVO importFriendShip(ImportFriendShipReq req);

    ResponseVO addFriend(AddFriendReq req);

    ResponseVO updateFriend(UpdateFriendReq req);

    ResponseVO deleteFriend(DeleteFriendReq req);

    ResponseVO deleteAllFriend(DeleteFriendReq req);

    ResponseVO getAllFriendShip(GetAllFriendShipReq req);

    ResponseVO getRelation(GetRelationReq req);

    ResponseVO doAddFriend(RequestBase requestBase, String fromId, FriendDto dto, Integer appId);

    ResponseVO checkFriendShip(CheckFriendShipReq req);

    ResponseVO addBlack(AddFriendShipBlackReq req);

    ResponseVO deleteBlack(DeleteBlackReq req);

    ResponseVO checkBlack(CheckFriendShipReq req);
}
