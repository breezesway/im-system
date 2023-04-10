package com.cgz.im.common.constant;

public class Constants {

    /**
     * channel绑定的userId key
     */
    public static final String UserId = "userId";

    /**
     * channel绑定的appId key
     */
    public static final String AppId = "appId";

    public static final String ClientType = "clientType";

    public static final String ReadTime = "readTime";

    public static class RedisConstants{
        /**
         * 用户session,appId+UserSessionConstants+用户id
         */
        public static final String UserSessionConstants = ":userSession:";
    }

}
