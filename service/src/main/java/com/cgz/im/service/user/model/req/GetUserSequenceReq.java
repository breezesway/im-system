package com.cgz.im.service.user.model.req;

import com.cgz.im.common.model.RequestBase;
import lombok.Data;

@Data
public class GetUserSequenceReq extends RequestBase {
    private String userId;
}
