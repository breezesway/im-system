package com.cgz.im.service.interceptor;

import com.alibaba.fastjson.JSONObject;
import com.cgz.im.common.BaseErrorCode;
import com.cgz.im.common.ResponseVO;
import com.cgz.im.common.enums.GateWayErrorCode;
import com.cgz.im.common.exception.ApplicationExceptionEnum;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;

@Component
public class GateWayInterceptor implements HandlerInterceptor {

    @Autowired
    IdentityCheck identityCheck;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //获取appId 操作人 userSign
        String appId = request.getParameter("appId");
        if(StringUtils.isBlank(appId)){
            resp(ResponseVO.errorResponse(GateWayErrorCode.APPID_NOT_EXIST),response);
            return false;
        }
        String identifier = request.getParameter("identifier");
        if(StringUtils.isBlank(identifier)){
            resp(ResponseVO.errorResponse(GateWayErrorCode.OPERATER_NOT_EXIST),response);
            return false;
        }
        String userSign = request.getParameter("userSign");
        if(StringUtils.isBlank(userSign)){
            resp(ResponseVO.errorResponse(GateWayErrorCode.USERSIGN_IS_ERROR),response);
            return false;
        }
        //签名和操作人和appid是否匹配
        ApplicationExceptionEnum applicationExceptionEnum = identityCheck.checkUserSig(identifier, appId, userSign);
        if(applicationExceptionEnum != BaseErrorCode.SUCCESS){
            resp(ResponseVO.errorResponse(applicationExceptionEnum),response);
            return false;
        }
        return false;
    }

    private void resp(ResponseVO responseVO, HttpServletResponse response){
        PrintWriter writer = null;
        response.setCharacterEncoding("UTF-8");
        response.setContentType("text/html; charset=utf-8");
        try {
            String resp = JSONObject.toJSONString(responseVO);
            writer = response.getWriter();
            writer.write(resp);
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            if(writer!=null){
                writer.checkError();
            }
        }
    }
}
