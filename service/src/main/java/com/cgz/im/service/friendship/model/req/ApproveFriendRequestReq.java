package com.cgz.im.service.friendship.model.req;

import com.cgz.im.common.model.RequestBase;
import lombok.Data;

@Data
public class ApproveFriendRequestReq extends RequestBase {

    private Long id;

    //1同意 2拒绝
    private Integer status;
}
