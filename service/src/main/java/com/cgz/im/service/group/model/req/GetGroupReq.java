package com.cgz.im.service.group.model.req;

import com.cgz.im.common.model.RequestBase;
import lombok.Data;

@Data
public class GetGroupReq extends RequestBase {

    private String groupId;

}
