package com.vuong.app.business.discord.service;

import com.vuong.app.business.discord.model.payload.GetMemberByServerIdRequest;
import com.vuong.app.business.discord.model.payload.GetMembersByServerIdRequest;
import com.vuong.app.common.api.ResponseObject;
import com.vuong.app.security.UserPrincipal;

public interface MemberService {
    ResponseObject getMembersByServerId(UserPrincipal currentUser, GetMembersByServerIdRequest request);

    ResponseObject getMemberByServerId(UserPrincipal currentUser, GetMemberByServerIdRequest request);

}
