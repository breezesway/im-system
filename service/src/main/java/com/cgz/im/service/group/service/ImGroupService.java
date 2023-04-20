package com.cgz.im.service.group.service;

import com.cgz.im.common.ResponseVO;
import com.cgz.im.common.model.SyncReq;
import com.cgz.im.service.group.dao.ImGroupEntity;
import com.cgz.im.service.group.model.req.*;

public interface ImGroupService {

    ResponseVO importGroup(ImportGroupReq req);

    ResponseVO createGroup(CreateGroupReq req);

    ResponseVO updateBaseGroupInfo(UpdateGroupReq req);

    ResponseVO getJoinedGroup(GetJoinedGroupReq req);

    ResponseVO destroyGroup(DestroyGroupReq req);

    ResponseVO transferGroup(TransferGroupReq req);

    ResponseVO<ImGroupEntity> getGroup(String groupId, Integer appId);

    ResponseVO getGroup(GetGroupReq req);

    ResponseVO muteGroup(MuteGroupReq req);

    ResponseVO syncJoinedGroupList(SyncReq req);

    Long getUserGroupMaxSeq(String userId, Integer appId);
}
