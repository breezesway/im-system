package com.cgz.im.service.user.model.req;

import com.cgz.im.common.model.RequestBase;
import com.cgz.im.service.user.dao.ImUserDataEntity;
import lombok.Data;

import java.util.List;

@Data
public class ImportUserReq extends RequestBase {

    private List<ImUserDataEntity> userData;
}
