package com.cgz.im.service.user.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.cgz.im.codec.pack.user.UserModifyPack;
import com.cgz.im.common.ResponseVO;
import com.cgz.im.common.config.AppConfig;
import com.cgz.im.common.constant.Constants;
import com.cgz.im.common.enums.DelFlagEnum;
import com.cgz.im.common.enums.UserErrorCode;
import com.cgz.im.common.enums.command.UserEventCommand;
import com.cgz.im.common.exception.ApplicationException;
import com.cgz.im.service.user.dao.ImUserDataEntity;
import com.cgz.im.service.user.dao.mapper.ImUserDataMapper;
import com.cgz.im.service.user.model.req.*;
import com.cgz.im.service.user.model.resp.GetUserInfoResp;
import com.cgz.im.service.user.model.resp.ImportUserResp;
import com.cgz.im.service.user.service.ImUserService;
import com.cgz.im.service.utils.CallbackService;
import com.cgz.im.service.utils.MessageProducer;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Service
public class ImUserServiceImpl implements ImUserService {

    @Autowired
    ImUserDataMapper imUserDataMapper;

    @Autowired
    AppConfig appConfig;

    @Autowired
    CallbackService callbackService;

    @Autowired
    MessageProducer messageProducer;

    @Override
    public ResponseVO importUser(ImportUserReq req) {
        if(req.getUserData().size()>100){
            ResponseVO.errorResponse(UserErrorCode.IMPORT_SIZE_TOO_LARGE);
        }
        List<String> successId = new ArrayList<>();
        List<String> errorId = new ArrayList<>();

        req.getUserData().forEach(e->{
            try {
                e.setAppId(req.getAppId());
                int insert = imUserDataMapper.insert(e);
                if(insert == 1){
                    successId.add(e.getUserId());
                }
            }catch (Exception ex){
                ex.printStackTrace();
                errorId.add(e.getUserId());
            }

        });
        ImportUserResp importUserResp = new ImportUserResp();
        importUserResp.setSuccessId(successId);
        importUserResp.setErrorId(errorId);
        return ResponseVO.successResponse(importUserResp);
    }

    @Override
    public ResponseVO<GetUserInfoResp> getUserInfo(GetUserInfoReq req) {
        QueryWrapper<ImUserDataEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("app_id",req.getAppId());
        queryWrapper.in("user_id",req.getUserIds());
        queryWrapper.eq("del_flag", DelFlagEnum.NORMAL.getCode());

        List<ImUserDataEntity> userDataEntities = imUserDataMapper.selectList(queryWrapper);
        HashMap<String, ImUserDataEntity> map = new HashMap<>();

        for (ImUserDataEntity data:
                userDataEntities) {
            map.put(data.getUserId(),data);
        }

        List<String> failUser = new ArrayList<>();
        for (String uid:
                req.getUserIds()) {
            if(!map.containsKey(uid)){
                failUser.add(uid);
            }
        }

        GetUserInfoResp resp = new GetUserInfoResp();
        resp.setUserDataItem(userDataEntities);
        resp.setFailUser(failUser);
        return ResponseVO.successResponse(resp);
    }

    @Override
    public ResponseVO<ImUserDataEntity> getSingleUserInfo(String userId, Integer appId) {
        QueryWrapper<ImUserDataEntity> objectQueryWrapper = new QueryWrapper<>();
        objectQueryWrapper.eq("app_id",appId);
        objectQueryWrapper.eq("user_id",userId);
        objectQueryWrapper.eq("del_flag", DelFlagEnum.NORMAL.getCode());

        ImUserDataEntity ImUserDataEntity = imUserDataMapper.selectOne(objectQueryWrapper);
        if(ImUserDataEntity == null){
            return ResponseVO.errorResponse(UserErrorCode.USER_NOT_EXIST);
        }

        return ResponseVO.successResponse(ImUserDataEntity);
    }

    @Override
    public ResponseVO deleteUser(DeleteUserReq req) {
        ImUserDataEntity entity = new ImUserDataEntity();
        entity.setDelFlag(DelFlagEnum.DELETE.getCode());

        List<String> errorId = new ArrayList<>();
        List<String> successId = new ArrayList<>();

        for (String userId : req.getUserId()) {
            QueryWrapper<ImUserDataEntity> wrapper = new QueryWrapper<>();
            wrapper.eq("app_id",req.getAppId());
            wrapper.eq("user_id",userId);
            wrapper.eq("del_flag",DelFlagEnum.NORMAL.getCode());
            int update;

            try {
                update =  imUserDataMapper.update(entity, wrapper);
                if(update > 0){
                    successId.add(userId);
                }else{
                    errorId.add(userId);
                }
            }catch (Exception e){
                errorId.add(userId);
            }
        }

        ImportUserResp resp = new ImportUserResp();
        resp.setSuccessId(successId);
        resp.setErrorId(errorId);
        return ResponseVO.successResponse(resp);
    }

    @Override
    @Transactional
    public ResponseVO modifyUserInfo(ModifyUserInfoReq req) {
        QueryWrapper<ImUserDataEntity> query = new QueryWrapper<>();
        query.eq("app_id",req.getAppId());
        query.eq("user_id",req.getUserId());
        query.eq("del_flag", DelFlagEnum.NORMAL.getCode());
        ImUserDataEntity user = imUserDataMapper.selectOne(query);
        if(user == null){
            throw new ApplicationException(UserErrorCode.USER_NOT_EXIST);
        }

        ImUserDataEntity update = new ImUserDataEntity();
        BeanUtils.copyProperties(req,update);

        update.setAppId(null);
        update.setUserId(null);
        int update1 = imUserDataMapper.update(update, query);
        if(update1 == 1){

            UserModifyPack pack = new UserModifyPack();
            BeanUtils.copyProperties(req,pack);
            System.out.println("modifyUserInfo发送消息");
            messageProducer.sendToUser(req.getUserId(),
                    req.getClientType(),
                    req.getImei(),
                    UserEventCommand.USER_MODIFY,
                    pack,
                    req.getAppId());

            if (appConfig.isModifyUserAfterCallback()){
                callbackService.callback(req.getAppId(),
                        Constants.CallbackCommand.ModifyUserAfter,
                        JSONObject.toJSONString(req));
            }
            return ResponseVO.successResponse();
        }
        throw new ApplicationException(UserErrorCode.MODIFY_USER_ERROR);
    }

    @Override
    public ResponseVO login(LoginReq req) {
        return ResponseVO.successResponse();
    }
}
