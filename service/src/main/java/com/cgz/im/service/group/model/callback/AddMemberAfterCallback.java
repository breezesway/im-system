package com.cgz.im.service.group.model.callback;

import com.cgz.im.service.group.model.resp.AddMemberResp;
import lombok.Data;

import java.util.List;

@Data
public class AddMemberAfterCallback {
    private String groupId;
    private Integer groupType;
    private String operator;
    private List<AddMemberResp> memberId;
}
