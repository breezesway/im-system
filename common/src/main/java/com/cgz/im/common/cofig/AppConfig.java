package com.cgz.im.common.cofig;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "appconfig")
public class AppConfig {

    /** zk连接地址*/
    private String zkAddr;

    /** zk连接超时时间*/
    private Integer zkConnectTimeOut;

    /** 负载均衡策略*/
    private Integer imRouteWay;

    /** 一致性哈希具体的哈希算法实现*/
    private Integer consistentHashWay;
}
