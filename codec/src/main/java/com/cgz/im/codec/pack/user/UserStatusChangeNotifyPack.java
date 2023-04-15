package com.cgz.im.codec.pack.user;

import com.cgz.im.common.model.UserSession;
import lombok.Data;

import java.util.List;

@Data
public class UserStatusChangeNotifyPack {

    private Integer appId;

    private String userId;

    private Integer status;

    private List<UserSession> client;

}
