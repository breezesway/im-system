package com.cgz.im.service.user.service;

import com.cgz.im.common.ResponseVO;
import com.cgz.im.service.user.model.UserStatusChangeNotifyContent;
/*import com.cgz.im.service.user.model.req.PullFriendOnlineStatusReq;
import com.cgz.im.service.user.model.req.PullUserOnlineStatusReq;
import com.cgz.im.service.user.model.req.SetUserCustomerStatusReq;
import com.cgz.im.service.user.model.req.SubscribeUserOnlineStatusReq;
import com.cgz.im.service.user.model.resp.UserOnlineStatusResp;*/

import java.util.Map;

public interface ImUserStatusService {

    void processUserOnlineStatusNotify(UserStatusChangeNotifyContent content);

    /*void subscribeUserOnlineStatus(SubscribeUserOnlineStatusReq req);

    void setUserCustomerStatus(SetUserCustomerStatusReq req);

    Map<String, UserOnlineStatusResp> queryFriendOnlineStatus(PullFriendOnlineStatusReq req);

    Map<String, UserOnlineStatusResp> queryUserOnlineStatus(PullUserOnlineStatusReq req);*/
}
