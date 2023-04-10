package com.cgz.im.service.friendship.service;

import com.cgz.im.common.ResponseVO;
import com.cgz.im.service.friendship.model.req.ApproveFriendRequestReq;
import com.cgz.im.service.friendship.model.req.FriendDto;
import com.cgz.im.service.friendship.model.req.ReadFriendShipRequestReq;


/**
 * 添加好友请求
 */
public interface ImFriendShipRequestService {

    ResponseVO addFriendShipRequest(String fromId, FriendDto dto, Integer appId);

    ResponseVO approveFriendRequest(ApproveFriendRequestReq req);

    ResponseVO readFriendShipRequestReq(ReadFriendShipRequestReq req);

    ResponseVO getFriendRequest(String fromId, Integer appId);
}
