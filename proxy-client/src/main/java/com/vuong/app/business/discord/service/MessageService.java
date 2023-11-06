package com.vuong.app.business.discord.service;

import com.vuong.app.business.discord.model.payload.CreateMessageRequest;
import com.vuong.app.business.discord.model.payload.DeleteMessageRequest;
import com.vuong.app.business.discord.model.payload.GetMessagesByChannelIdRequest;
import com.vuong.app.business.discord.model.payload.UpdateMessageRequest;
import com.vuong.app.common.api.ResponseObject;
import com.vuong.app.security.UserPrincipal;

public interface MessageService {
    ResponseObject createMessage(UserPrincipal currentUser, CreateMessageRequest request);

    ResponseObject updateMessage(UserPrincipal currentUser, UpdateMessageRequest request);

    ResponseObject deleteMessage(UserPrincipal currentUser, DeleteMessageRequest request);

    ResponseObject getMessagesByChannelId(UserPrincipal currentUser, GetMessagesByChannelIdRequest request);
}
