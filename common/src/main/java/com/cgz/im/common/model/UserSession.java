package com.cgz.im.common.model;

import lombok.Data;

/**
 * 该类与UserClientDto的区别是：
 * 该类代表一个用户一台设备的一个session
 * UserClientDto起唯一标识作用
 */
@Data
public class UserSession {

    private String userId;

    /**
     * 应用ID
     */
    private Integer appId;

    /**
     * 端的标识
     */
    private Integer clientType;

    //sdk 版本号
    private Integer version;

    //连接状态 1=在线 2=离线
    private Integer connectState;

    private Integer brokerId;

    private String brokerHost;

    private String imei;

}
