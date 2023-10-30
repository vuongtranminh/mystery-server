package com.vuong.app.business.discord.service;

import com.vuong.app.business.discord.model.payload.CreateServerRequest;
import com.vuong.app.business.discord.model.payload.DeleteServerRequest;
import com.vuong.app.business.discord.model.payload.UpdateServerRequest;
import com.vuong.app.common.api.ResponseObject;
import com.vuong.app.security.UserPrincipal;

public interface ServerService {
    ResponseObject createServer(CreateServerRequest request, UserPrincipal currentUser);
    ResponseObject getServerByInviteCode(String inviteCode);
    ResponseObject getServerByServerId(Integer serverId);
    ResponseObject deleteServerByServerId(DeleteServerRequest request, UserPrincipal currentUser);
    ResponseObject updateServerByServerId(UpdateServerRequest request, UserPrincipal currentUser);
    ResponseObject getFirstServer(UserPrincipal currentUser);
}
