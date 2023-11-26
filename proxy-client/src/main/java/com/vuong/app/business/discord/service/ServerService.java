package com.vuong.app.business.discord.service;

import com.vuong.app.business.discord.model.payload.*;
import com.vuong.app.common.api.ResponseObject;
import com.vuong.app.security.UserPrincipal;
import com.vuong.app.v1.discord.GrpcLeaveServerRequest;
import com.vuong.app.v1.discord.GrpcLeaveServerResponse;

import java.util.Optional;

public interface ServerService {
    ResponseObject createServer(UserPrincipal currentUser, CreateServerRequest request);

    ResponseObject getServersJoin(UserPrincipal currentUser, GetServersJoinRequest request);

    ResponseObject getFirstServerJoin(UserPrincipal currentUser);

    ResponseObject getServerJoinByServerId(UserPrincipal currentUser, GetServerJoinByServerIdRequest request);

    ResponseObject joinServerByInviteCode(UserPrincipal currentUser, JoinServerByInviteCodeRequest request);

    ResponseObject leaveServer(UserPrincipal currentUser, LeaveServerRequest request);
}
