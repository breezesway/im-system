package com.cgz.im.service.utils;

import com.alibaba.fastjson.JSONObject;
import com.cgz.im.common.constant.Constants;
import com.cgz.im.common.enums.ImConnectStatusEnum;
import com.cgz.im.common.model.UserSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

@Component
public class UserSessionUtils {

    @Autowired
    StringRedisTemplate stringRedisTemplate;

    //获取该用户所有在线的session
    public List<UserSession> getUserSession(Integer appId,String userId){
        String userSessionKey = appId + Constants.RedisConstants.UserSessionConstants
                + userId;
        Map<Object, Object> entries = stringRedisTemplate.opsForHash().entries(userSessionKey);
        List<UserSession> list = new ArrayList<>();
        Collection<Object> values = entries.values();
        for (Object o : values){
            String str = (String) o;
            UserSession session = JSONObject.parseObject(str, UserSession.class);
            if(session.getConnectState().equals(ImConnectStatusEnum.ONLINE_STATUS.getCode())){
                list.add(session);
            }
        }
        return list;
    }

    //获取某用户指定端的session
    public UserSession getUserSession(Integer appId,String userId,Integer clientType,String imei){

        String userSessionKey = appId + Constants.RedisConstants.UserSessionConstants
                + userId;
        String hashKey = clientType + ":" + imei;
        Object o = stringRedisTemplate.opsForHash().get(userSessionKey, hashKey);
        UserSession session = JSONObject.parseObject(o.toString(), UserSession.class);
        return session;
    }
}
