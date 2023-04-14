package com.cgz.im.common.config;

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

    /** 回调地址*/
    private String callbackUrl;

    private boolean modifyUserAfterCallback; //用户资料变更之后回调开关

    private boolean addFriendAfterCallback; //添加好友之后回调开关

    private boolean addFriendBeforeCallback; //添加好友之前回调开关

    private boolean modifyFriendAfterCallback; //修改好友之后回调开关

    private boolean deleteFriendAfterCallback; //删除好友之后回调开关

    private boolean addFriendShipBlackAfterCallback; //添加黑名单之后回调开关

    private boolean deleteFriendShipBlackAfterCallback; //删除黑名单之后回调开关

    private boolean createGroupAfterCallback; //创建群聊之后回调开关

    private boolean modifyGroupAfterCallback; //修改群聊之后回调开关

    private boolean destroyGroupAfterCallback;//解散群聊之后回调开关

    private boolean deleteGroupMemberAfterCallback;//删除群成员之后回调

    private boolean addGroupMemberBeforeCallback;//拉人入群之前回调

    private boolean addGroupMemberAfterCallback;//拉人入群之后回调
}
