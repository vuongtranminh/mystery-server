package com.vuong.app.business.discord.service.impl;

import com.vuong.app.business.Meta;
import com.vuong.app.business.discord.model.MemberProfile;
import com.vuong.app.business.discord.model.MemberRole;
import com.vuong.app.business.discord.model.Message;
import com.vuong.app.business.discord.model.payload.*;
import com.vuong.app.business.discord.service.MessageService;
import com.vuong.app.common.api.ResponseMsg;
import com.vuong.app.common.api.ResponseObject;
import com.vuong.app.grpc.service.MessageClientService;
import com.vuong.app.security.UserPrincipal;
import com.vuong.app.v1.discord.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MessageServiceImpl implements MessageService {

    private final MessageClientService messageClientService;

    @Override
    public ResponseObject createMessage(UserPrincipal currentUser, CreateMessageRequest request) {
        GrpcCreateMessageResponse grpcResponse = this.messageClientService.createMessage(GrpcCreateMessageRequest.newBuilder()
                .setContent(request.getContent())
                .setFileUrl(request.getFileUrl())
                .setChannelId(request.getChannelId())
                .setProfileId(currentUser.getUserId())
                .build());

        return new ResponseMsg("Sign up successfully!", HttpStatus.OK, CreateMessageResponse.builder()
                .messageId(grpcResponse.getMessageId())
                .build());
    }

    @Override
    public ResponseObject updateMessage(UserPrincipal currentUser, UpdateMessageRequest request) {
        GrpcUpdateMessageResponse grpcResponse = this.messageClientService.updateMessage(GrpcUpdateMessageRequest.newBuilder()
                .setMessageId(request.getMessageId())
                .setContent(request.getContent())
                .setProfileId(currentUser.getUserId())
                .build());

        return new ResponseMsg("Sign up successfully!", HttpStatus.OK, UpdateMessageResponse.builder()
                .messageId(grpcResponse.getMessageId())
                .build());
    }

    @Override
    public ResponseObject deleteMessage(UserPrincipal currentUser, DeleteMessageRequest request) {
        GrpcDeleteMessageResponse grpcResponse = this.messageClientService.deleteMessage(GrpcDeleteMessageRequest.newBuilder()
                .setMessageId(request.getMessageId())
                .setProfileId(currentUser.getUserId())
                .build());

        return new ResponseMsg("Sign up successfully!", HttpStatus.OK, DeleteMessageResponse.builder()
                .deleted(grpcResponse.getDeleted())
                .build());
    }

    @Override
    public ResponseObject getMessagesByChannelId(UserPrincipal currentUser, GetMessagesByChannelIdRequest request) {
        GrpcGetMessagesByChannelIdResponse grpcResponse = this.messageClientService.getMessagesByChannelId(GrpcGetMessagesByChannelIdRequest.newBuilder()
                .setChannelId(request.getChannelId())
                .setProfileId(currentUser.getUserId())
                .setPageNumber(request.getPageNumber())
                .setPageSize(request.getPageSize())
                .build());

        return new ResponseMsg("Sign up successfully!", HttpStatus.OK, GetMessagesByChannelIdResponse.builder()
                .meta(Meta.parse(grpcResponse.getMeta()))
                .content(grpcResponse.getContentList().stream().map(grpcMessage -> Message.builder()
                        .messageId(grpcMessage.getMessageId())
                        .content(grpcMessage.getContent())
                        .fileUrl(grpcMessage.getFileUrl())
                        .channelId(grpcMessage.getChannelId())
                        .createdAt(grpcMessage.getCreatedAt())
                        .updatedAt(grpcMessage.getUpdatedAt())
                        .deletedAt(grpcMessage.getDeletedAt())
                        .deletedBy(grpcMessage.getDeletedBy())
                        .author(MemberProfile.builder()
                                .memberId(grpcMessage.getAuthor().getMemberId())
                                .role(MemberRole.forNumber(grpcMessage.getAuthor().getRole().getNumber()))
                                .serverId(grpcMessage.getAuthor().getServerId())
                                .joinAt(grpcMessage.getAuthor().getJoinAt())
                                .profileId(grpcMessage.getAuthor().getProfileId())
                                .name(grpcMessage.getAuthor().getName())
                                .avtUrl(grpcMessage.getAuthor().getAvtUrl())
                                .build())
                        .build())
                        .collect(Collectors.toUnmodifiableList()))
                .build());
    }
}
