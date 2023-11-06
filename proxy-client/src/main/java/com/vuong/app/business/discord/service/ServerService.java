package com.vuong.app.business.discord.service;

import com.vuong.app.business.discord.model.payload.CreateServerRequest;
import com.vuong.app.business.discord.model.payload.GetServersJoinRequest;
import com.vuong.app.common.api.ResponseObject;
import com.vuong.app.security.UserPrincipal;

public interface ServerService {
    ResponseObject createServer(UserPrincipal currentUser, CreateServerRequest request);

    ResponseObject getServersJoin(UserPrincipal currentUser, GetServersJoinRequest request);
}
