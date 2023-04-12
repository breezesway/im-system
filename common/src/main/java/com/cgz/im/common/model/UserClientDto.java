package com.cgz.im.common.model;

import lombok.Data;

/**
 * 标识作用
 * 这4个值可唯一标识一个用户的一台设备
 */
@Data
public class UserClientDto {

    private Integer appId;

    private Integer clientType;

    private String userId;

    private String imei;

}
