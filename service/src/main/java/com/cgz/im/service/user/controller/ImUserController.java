package com.cgz.im.service.user.controller;

import com.cgz.im.common.ClientType;
import com.cgz.im.common.ResponseVO;
import com.cgz.im.common.route.RouteInfo;
import com.cgz.im.common.route.RouterHandle;
import com.cgz.im.common.utils.RouteInfoParseUtil;
import com.cgz.im.service.user.model.req.*;
import com.cgz.im.service.user.service.ImUserService;
import com.cgz.im.service.user.service.ImUserStatusService;
import com.cgz.im.service.utils.ZKit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("v1/user")
public class ImUserController {

    @Autowired
    ImUserService imUserService;

    @Autowired
    RouterHandle routerHandle;

    @Autowired
    ZKit zKit;

    @Autowired
    ImUserStatusService imUserStatusService;

    @RequestMapping("importUser")
    public ResponseVO importUser(@RequestBody ImportUserReq req, Integer appId){
        req.setAppId(appId);
        return imUserService.importUser(req);
    }

    /**
     * im登录接口，返回im地址
     */
    @RequestMapping("login")
    public ResponseVO login(@RequestBody LoginReq req, Integer appId){
        req.setAppId(appId);
        ResponseVO login = imUserService.login(req);
        if (login.isOk()){
            List<String> allNode;
            if(req.getClientType() == ClientType.WEB.getCode()){
                allNode = zKit.getAllWebNode();
            }else{
                allNode = zKit.getAllTcpNode();
            }
            String s = routerHandle.routeServer(allNode, req.getUserId());
            RouteInfo parse = RouteInfoParseUtil.parse(s);
            return ResponseVO.successResponse(parse);
        }
        return ResponseVO.errorResponse();
    }

    @RequestMapping("/getUserSequence")
    public ResponseVO getUserSequence(@RequestBody @Validated
                                              GetUserSequenceReq req, Integer appId) {
        req.setAppId(appId);
        return imUserService.getUserSequence(req);
    }

    @RequestMapping("/subscribeUserOnlineStatus")
    public ResponseVO subscribeUserOnlineStatus(@RequestBody @Validated
                                                        SubscribeUserOnlineStatusReq req, Integer appId, String identifier) {
        req.setAppId(appId);
        req.setOperator(identifier);
        imUserStatusService.subscribeUserOnlineStatus(req);
        return ResponseVO.successResponse();
    }

    @RequestMapping("/setUserCustomerStatus")
    public ResponseVO setUserCustomerStatus(@RequestBody @Validated
                                                    SetUserCustomerStatusReq req, Integer appId, String identifier) {
        req.setAppId(appId);
        req.setOperator(identifier);
        imUserStatusService.setUserCustomerStatus(req);
        return ResponseVO.successResponse();
    }

    @RequestMapping("/queryFriendOnlineStatus")
    public ResponseVO queryFriendOnlineStatus(@RequestBody @Validated
                                                      PullFriendOnlineStatusReq req, Integer appId,String identifier) {
        req.setAppId(appId);
        req.setOperator(identifier);
        return ResponseVO.successResponse(imUserStatusService.queryFriendOnlineStatus(req));
    }

    @RequestMapping("/queryUserOnlineStatus")
    public ResponseVO queryUserOnlineStatus(@RequestBody @Validated
                                                    PullUserOnlineStatusReq req, Integer appId,String identifier) {
        req.setAppId(appId);
        req.setOperator(identifier);
        return ResponseVO.successResponse(imUserStatusService.queryUserOnlineStatus(req));
    }

}
