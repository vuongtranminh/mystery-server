package com.vuong.app.business.discord.service.impl;

import com.vuong.app.business.Meta;
import com.vuong.app.business.discord.model.Channel;
import com.vuong.app.business.discord.model.ChannelType;
import com.vuong.app.business.discord.model.payload.*;
import com.vuong.app.business.discord.service.ChannelService;
import com.vuong.app.common.api.ResponseMsg;
import com.vuong.app.common.api.ResponseObject;
import com.vuong.app.exception.wrapper.ResourceNotFoundException;
import com.vuong.app.grpc.service.ChannelClientService;
import com.vuong.app.security.UserPrincipal;
import com.vuong.app.v1.discord.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class ChannelServiceImpl implements ChannelService {

    private final ChannelClientService channelClientService;

    @Override
    public ResponseObject createChannel(UserPrincipal currentUser, CreateChannelRequest request) {
        GrpcCreateChannelResponse grpcResponse = this.channelClientService.createChannel(GrpcCreateChannelRequest.newBuilder()
                .setProfileId(currentUser.getUserId())
                .setServerId(request.getServerId())
                .setName(request.getName())
                .setType(GrpcChannelType.forNumber(request.getType()))
                .build());

        return new ResponseMsg("Sign up successfully!", HttpStatus.OK, CreateChannelResponse.builder()
                .channelId(grpcResponse.getChannelId())
                .build());
    }

    @Override
    public ResponseObject updateChannel(UserPrincipal currentUser, UpdateChannelRequest request) {
        GrpcUpdateChannelResponse grpcResponse = this.channelClientService.updateChannel(GrpcUpdateChannelRequest.newBuilder()
                .setProfileId(currentUser.getUserId())
                .setChannelId(request.getChannelId())
                .setName(request.getName())
                .build());

        return new ResponseMsg("Sign up successfully!", HttpStatus.OK, UpdateChannelResponse.builder()
                .channelId(grpcResponse.getChannelId())
                .build());
    }

    @Override
    public ResponseObject deleteChannel(UserPrincipal currentUser, DeleteChannelRequest request) {
        GrpcDeleteChannelResponse grpcResponse = this.channelClientService.deleteChannel(GrpcDeleteChannelRequest.newBuilder()
                .setProfileId(currentUser.getUserId())
                .setChannelId(request.getChannelId())
                .build());

        return new ResponseMsg("Sign up successfully!", HttpStatus.OK, DeleteChannelResponse.builder()
                .deleted(grpcResponse.getDeleted())
                .build());
    }

    @Override
    public ResponseObject getChannelGeneralByServerId(UserPrincipal currentUser, GetChannelGeneralByServerIdRequest request) {
        GrpcGetChannelGeneralByServerIdResponse grpcResponse = this.channelClientService.getChannelGeneralByServerId(GrpcGetChannelGeneralByServerIdRequest.newBuilder()
                .setProfileId(currentUser.getUserId())
                .setServerId(request.getServerId())
                .build()).orElseThrow(() -> new ResourceNotFoundException("channel", "serverId", request.getServerId()));

        return new ResponseMsg("Sign up successfully!", HttpStatus.OK, GetChannelGeneralByServerIdResponse.builder()
                .channelId(grpcResponse.getResult().getChannelId())
                .name(grpcResponse.getResult().getName())
                .type(ChannelType.forNumber(grpcResponse.getResult().getType().getNumber()).getNumber())
                .serverId(grpcResponse.getResult().getServerId())
                .createdAt(grpcResponse.getResult().getCreatedAt())
                .updatedAt(grpcResponse.getResult().getUpdatedAt())
                .build());
    }

    @Override
    public ResponseObject getChannelByChannelId(UserPrincipal currentUser, GetChannelByChannelIdRequest request) {
        GrpcGetChannelByChannelIdResponse grpcResponse = this.channelClientService.getChannelByChannelId(GrpcGetChannelByChannelIdRequest.newBuilder()
                .setProfileId(currentUser.getUserId())
                .setChannelId(request.getChannelId())
                .build()).orElseThrow(() -> new ResourceNotFoundException("channel", "channelId", request.getChannelId()));

        return new ResponseMsg("Sign up successfully!", HttpStatus.OK, GetChannelByChannelIdResponse.builder()
                .channelId(grpcResponse.getResult().getChannelId())
                .name(grpcResponse.getResult().getName())
                .type(ChannelType.forNumber(grpcResponse.getResult().getType().getNumber()).getNumber())
                .serverId(grpcResponse.getResult().getServerId())
                .createdAt(grpcResponse.getResult().getCreatedAt())
                .updatedAt(grpcResponse.getResult().getUpdatedAt())
                .build());
    }

    @Override
    public ResponseObject getChannelsByServerId(UserPrincipal currentUser, GetChannelsByServerIdRequest request) {
        GrpcGetChannelsByServerIdResponse grpcResponse = this.channelClientService.getChannelsByServerId(GrpcGetChannelsByServerIdRequest.newBuilder()
                .setProfileId(currentUser.getUserId())
                .setServerId(request.getServerId())
                .setPageNumber(request.getPageNumber())
                .setPageSize(request.getPageSize())
                .build());

        return new ResponseMsg("Sign up successfully!", HttpStatus.OK, GetChannelsByServerIdResponse.builder()
                .meta(Meta.parse(grpcResponse.getMeta()))
                .content(grpcResponse.getContentList().stream().map(grpcChannel -> Channel.builder()
                        .channelId(grpcChannel.getChannelId())
                        .name(grpcChannel.getName())
                        .type(ChannelType.forNumber(grpcChannel.getType().getNumber()).getNumber())
                        .serverId(grpcChannel.getServerId())
                        .createdAt(grpcChannel.getCreatedAt())
                        .updatedAt(grpcChannel.getUpdatedAt())
                        .build())
                        .collect(Collectors.toUnmodifiableList()))
                .build());
    }
}
