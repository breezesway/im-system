package com.cgz.im.service.user.model.req;

import com.cgz.im.common.model.RequestBase;
import lombok.Data;

import java.util.List;

@Data
public class SubscribeUserOnlineStatusReq extends RequestBase {

    private List<String> subUserId;

    private Long subTime;


}
