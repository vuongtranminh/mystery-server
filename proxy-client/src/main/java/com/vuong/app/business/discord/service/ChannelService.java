package com.vuong.app.business.discord.service;

import com.vuong.app.business.discord.model.payload.*;
import com.vuong.app.common.api.ResponseObject;
import com.vuong.app.security.UserPrincipal;

public interface ChannelService {
    ResponseObject createChannel(UserPrincipal currentUser, CreateChannelRequest request);

    ResponseObject updateChannel(UserPrincipal currentUser, UpdateChannelRequest request);

    ResponseObject deleteChannel(UserPrincipal currentUser, DeleteChannelRequest request);

    ResponseObject getChannelGeneralByServerId(UserPrincipal currentUser, GetChannelGeneralByServerIdRequest request);

    ResponseObject getChannelByChannelId(UserPrincipal currentUser, GetChannelByChannelIdRequest request);

    ResponseObject getChannelsByServerId(UserPrincipal currentUser, GetChannelsByServerIdRequest request);
}
