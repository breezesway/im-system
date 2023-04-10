package com.cgz.im.service.group.model.resp;

import com.cgz.im.service.group.dao.ImGroupEntity;
import lombok.Data;

import java.util.List;

@Data
public class GetJoinedGroupResp {

    private Integer totalCount;

    private List<ImGroupEntity> groupList;

}
