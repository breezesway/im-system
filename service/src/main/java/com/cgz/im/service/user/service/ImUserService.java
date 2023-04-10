package com.cgz.im.service.user.service;

import com.cgz.im.common.ResponseVO;
import com.cgz.im.service.user.dao.ImUserDataEntity;
import com.cgz.im.service.user.model.req.DeleteUserReq;
import com.cgz.im.service.user.model.req.GetUserInfoReq;
import com.cgz.im.service.user.model.req.ImportUserReq;
import com.cgz.im.service.user.model.req.ModifyUserInfoReq;
import com.cgz.im.service.user.model.resp.GetUserInfoResp;

public interface ImUserService {

    ResponseVO importUser(ImportUserReq req);

    ResponseVO<GetUserInfoResp> getUserInfo(GetUserInfoReq req);

    ResponseVO<ImUserDataEntity> getSingleUserInfo(String userId, Integer appId);

    ResponseVO deleteUser(DeleteUserReq req);

    ResponseVO modifyUserInfo(ModifyUserInfoReq req);
}
