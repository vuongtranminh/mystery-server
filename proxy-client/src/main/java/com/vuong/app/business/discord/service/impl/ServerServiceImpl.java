package com.vuong.app.business.discord.service.impl;

import com.vuong.app.business.auth.model.AuthProvider;
import com.vuong.app.business.auth.model.payload.SignUpResponse;
import com.vuong.app.business.discord.model.payload.CreateServerRequest;
import com.vuong.app.business.discord.model.payload.DeleteServerRequest;
import com.vuong.app.business.discord.model.payload.GetServerResponse;
import com.vuong.app.business.discord.model.payload.UpdateServerRequest;
import com.vuong.app.business.discord.service.ServerService;
import com.vuong.app.common.api.ResponseMsg;
import com.vuong.app.common.api.ResponseObject;
import com.vuong.app.exception.wrapper.ResourceNotFoundException;
import com.vuong.app.grpc.message.discord.*;
import com.vuong.app.grpc.service.ServerClientService;
import com.vuong.app.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class ServerServiceImpl implements ServerService {

    private final ServerClientService serverClientService;

    @Override
    public ResponseObject createServer(CreateServerRequest request, UserPrincipal currentUser) {
        MessageCreateServerResponse messageCreateServerResponse = this.serverClientService.createServer(MessageCreateServerRequest.builder()
                        .name(request.getName())
                        .imgUrl(request.getImgUrl())
                        .profileId(currentUser.getUserId())
                .build());

        return new ResponseMsg("Sign up successfully!", HttpStatus.OK, messageCreateServerResponse.getServerId());
    }

    @Override
    public ResponseObject getServerByInviteCode(String inviteCode) {
        MessageGetServerByInviteCodeResponse messageGetServerByInviteCodeResponse = this.serverClientService.getServerByInviteCode(MessageGetServerByInviteCodeRequest.builder()
                        .inviteCode(inviteCode)
                .build()).orElseThrow(() -> new ResourceNotFoundException("server", "inviteCode", inviteCode));

        return new ResponseMsg("Sign up successfully!", HttpStatus.OK, GetServerResponse.builder()
                .serverId(messageGetServerByInviteCodeResponse.getServerId())
                .name(messageGetServerByInviteCodeResponse.getName())
                .imgUrl(messageGetServerByInviteCodeResponse.getImgUrl())
                .inviteCode(messageGetServerByInviteCodeResponse.getInviteCode())
                .profileId(messageGetServerByInviteCodeResponse.getProfileId())
                .createdAt(messageGetServerByInviteCodeResponse.getCreatedAt())
                .updatedAt(messageGetServerByInviteCodeResponse.getUpdatedAt())
                .build());
    }

    @Override
    public ResponseObject getServerByServerId(Integer serverId) {
        MessageGetServerByServerIdResponse messageGetServerByServerIdResponse = this.serverClientService.getServerByServerId(MessageGetServerByServerIdRequest.builder()
                .serverId(serverId)
                .build()).orElseThrow(() -> new ResourceNotFoundException("server", "serverId", serverId));

        return new ResponseMsg("Sign up successfully!", HttpStatus.OK, GetServerResponse.builder()
                .serverId(messageGetServerByServerIdResponse.getServerId())
                .name(messageGetServerByServerIdResponse.getName())
                .imgUrl(messageGetServerByServerIdResponse.getImgUrl())
                .inviteCode(messageGetServerByServerIdResponse.getInviteCode())
                .profileId(messageGetServerByServerIdResponse.getProfileId())
                .createdAt(messageGetServerByServerIdResponse.getCreatedAt())
                .updatedAt(messageGetServerByServerIdResponse.getUpdatedAt())
                .build());
    }

    @Override
    public ResponseObject deleteServerByServerId(DeleteServerRequest request, UserPrincipal currentUser) {
        return null;
    }

    @Override
    public ResponseObject updateServerByServerId(UpdateServerRequest request, UserPrincipal currentUser) {
        return null;
    }

}
