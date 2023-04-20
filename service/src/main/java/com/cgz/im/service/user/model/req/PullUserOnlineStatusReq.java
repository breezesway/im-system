package com.cgz.im.service.user.model.req;

import com.cgz.im.common.model.RequestBase;
import lombok.Data;

import java.util.List;

@Data
public class PullUserOnlineStatusReq extends RequestBase {

    private List<String> userList;

}
