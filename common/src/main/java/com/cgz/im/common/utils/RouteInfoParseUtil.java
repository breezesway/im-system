package com.cgz.im.common.utils;

import com.cgz.im.common.BaseErrorCode;
import com.cgz.im.common.exception.ApplicationException;
import com.cgz.im.common.route.RouteInfo;

/**
 * 将IP：PORT解析为RouteInfo实体
 */
public class RouteInfoParseUtil {

    public static RouteInfo parse(String info){
        try {
            String[] serverInfo = info.split(":");
            RouteInfo routeInfo =  new RouteInfo(serverInfo[0], Integer.parseInt(serverInfo[1])) ;
            return routeInfo ;
        }catch (Exception e){
            throw new ApplicationException(BaseErrorCode.PARAMETER_ERROR) ;
        }
    }
}
