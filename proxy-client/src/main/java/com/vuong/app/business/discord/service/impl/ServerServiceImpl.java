package com.vuong.app.business.discord.service.impl;

import com.vuong.app.business.Meta;
import com.vuong.app.business.discord.model.MemberProfile;
import com.vuong.app.business.discord.model.MemberRole;
import com.vuong.app.business.discord.model.Server;
import com.vuong.app.business.discord.model.payload.*;
import com.vuong.app.business.discord.service.ServerService;
import com.vuong.app.common.api.ResponseMsg;
import com.vuong.app.common.api.ResponseObject;
import com.vuong.app.exception.wrapper.ResourceNotFoundException;
import com.vuong.app.grpc.service.ServerClientService;
import com.vuong.app.security.UserPrincipal;
import com.vuong.app.v1.discord.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ServerServiceImpl implements ServerService {

    private final ServerClientService serverClientService;

    @Override
    public ResponseObject createServer(UserPrincipal currentUser, CreateServerRequest request) {
        GrpcCreateServerResponse grpcResponse = this.serverClientService.createServer(GrpcCreateServerRequest.newBuilder()
                .setName(request.getName())
                .setImgUrl(request.getImgUrl())
                .setAuthorId(currentUser.getUserId())
                .build());

        return new ResponseMsg("Sign up successfully!", HttpStatus.OK, CreateServerResponse.builder()
                .serverId(grpcResponse.getServerId())
                .build());
    }

    @Override
    public ResponseObject getServersJoin(UserPrincipal currentUser, GetServersJoinRequest request) {
        GrpcGetServersJoinResponse grpcResponse = this.serverClientService.getServersJoin(GrpcGetServersJoinRequest.newBuilder()
                .setProfileId(currentUser.getUserId())
                .setPageNumber(request.getPageNumber())
                .setPageSize(request.getPageSize())
                .build());

        return new ResponseMsg("Sign up successfully!", HttpStatus.OK, GetServersJoinResponse.builder()
                .meta(Meta.parse(grpcResponse.getMeta()))
                .content(grpcResponse.getContentList().stream().map(grpcServer -> Server.builder()
                        .serverId(grpcServer.getServerId())
                        .name(grpcServer.getName())
                        .imgUrl(grpcServer.getImgUrl())
                        .inviteCode(grpcServer.getInviteCode())
                        .authorId(grpcServer.getAuthorId())
                        .createdAt(grpcServer.getCreatedAt())
                        .updatedAt(grpcServer.getUpdatedAt())
                        .build())
                        .collect(Collectors.toUnmodifiableList()))
                .build());
    }

    @Override
    public ResponseObject getFirstServerJoin(UserPrincipal currentUser) {
        GrpcGetFirstServerJoinResponse grpcResponse = this.serverClientService.getFirstServerJoin(GrpcGetFirstServerJoinRequest.newBuilder()
                        .setProfileId(currentUser.getUserId())
                        .build())
                .orElseThrow(() -> new ResourceNotFoundException("server", "profileId", currentUser.getUserId()));

        return new ResponseMsg("Sign up successfully!", HttpStatus.OK, GetFirstServerJoinResponse.builder()
                .serverId(grpcResponse.getResult().getServerId())
                .name(grpcResponse.getResult().getName())
                .imgUrl(grpcResponse.getResult().getImgUrl())
                .inviteCode(grpcResponse.getResult().getInviteCode())
                .authorId(grpcResponse.getResult().getAuthorId())
                .createdAt(grpcResponse.getResult().getCreatedAt())
                .updatedAt(grpcResponse.getResult().getUpdatedAt())
                .build());
    }

    @Override
    public ResponseObject getServerJoinByServerId(UserPrincipal currentUser, GetServerJoinByServerIdRequest request) {
        GrpcGetServerJoinByServerIdResponse grpcResponse = this.serverClientService.getServerJoinByServerId(GrpcGetServerJoinByServerIdRequest.newBuilder()
                .setProfileId(currentUser.getUserId())
                .setServerId(request.getServerId())
                .build())
                .orElseThrow(() -> new ResourceNotFoundException("server", "serverId", request.getServerId()));

        return new ResponseMsg("Sign up successfully!", HttpStatus.OK, GetServerJoinByServerIdResponse.builder()
                .serverId(grpcResponse.getResult().getServerId())
                .name(grpcResponse.getResult().getName())
                .imgUrl(grpcResponse.getResult().getImgUrl())
                .inviteCode(grpcResponse.getResult().getInviteCode())
                .authorId(grpcResponse.getResult().getAuthorId())
                .createdAt(grpcResponse.getResult().getCreatedAt())
                .updatedAt(grpcResponse.getResult().getUpdatedAt())
                .build());
    }

    @Override
    public ResponseObject joinServerByInviteCode(UserPrincipal currentUser, JoinServerByInviteCodeRequest request) {
        GrpcJoinServerByInviteCodeResponse grpcResponse = this.serverClientService.joinServerByInviteCode(GrpcJoinServerByInviteCodeRequest.newBuilder()
                        .setProfileId(currentUser.getUserId())
                        .setInviteCode(request.getInviteCode())
                        .build())
                .orElseThrow(() -> new ResourceNotFoundException("server", "inviteCode", request.getInviteCode()));

        return new ResponseMsg("Sign up successfully!", HttpStatus.OK, JoinServerByInviteCodeResponse.builder()
                .serverId(grpcResponse.getServerId())
                .build());
    }

    @Override
    public ResponseObject leaveServer(UserPrincipal currentUser, LeaveServerRequest request) {
        GrpcLeaveServerResponse grpcResponse = this.serverClientService.leaveServer(GrpcLeaveServerRequest.newBuilder()
                        .setProfileId(currentUser.getUserId())
                        .setServerId(request.getServerId())
                        .build())
                .orElse(null);
        if (grpcResponse != null) {
            // public redis leaver member
        }
        return new ResponseMsg("Leave server successfully!", HttpStatus.OK);
    }


//    private final ServerClientService serverClientService;

//    @Override
//    public ResponseObject createServer(CreateServerRequest request, UserPrincipal currentUser) {
//        MessageCreateServerResponse messageCreateServerResponse = this.serverClientService.createServer(MessageCreateServerRequest.builder()
//                        .name(request.getName())
//                        .imgUrl(request.getImgUrl())
//                        .profileId(currentUser.getUserId())
//                .build());
//
//        return new ResponseMsg("Sign up successfully!", HttpStatus.OK, messageCreateServerResponse.getServerId());
//    }
//
//    @Override
//    public ResponseObject getServerByInviteCode(String inviteCode) {
//        MessageGetServerByInviteCodeResponse messageGetServerByInviteCodeResponse = this.serverClientService.getServerByInviteCode(MessageGetServerByInviteCodeRequest.builder()
//                        .inviteCode(inviteCode)
//                .build()).orElseThrow(() -> new ResourceNotFoundException("server", "inviteCode", inviteCode));
//
//        return new ResponseMsg("Sign up successfully!", HttpStatus.OK, GetServerResponse.builder()
//                .serverId(messageGetServerByInviteCodeResponse.getServerId())
//                .name(messageGetServerByInviteCodeResponse.getName())
//                .imgUrl(messageGetServerByInviteCodeResponse.getImgUrl())
//                .inviteCode(messageGetServerByInviteCodeResponse.getInviteCode())
//                .profileId(messageGetServerByInviteCodeResponse.getProfileId())
//                .createdAt(messageGetServerByInviteCodeResponse.getCreatedAt())
//                .updatedAt(messageGetServerByInviteCodeResponse.getUpdatedAt())
//                .build());
//    }
//
//    @Override
//    public ResponseObject getServerByServerId(Integer serverId) {
//        MessageGetServerByServerIdResponse messageGetServerByServerIdResponse = this.serverClientService.getServerByServerId(MessageGetServerByServerIdRequest.builder()
//                .serverId(serverId)
//                .build()).orElseThrow(() -> new ResourceNotFoundException("server", "serverId", serverId));
//
//        return new ResponseMsg("Sign up successfully!", HttpStatus.OK, GetServerResponse.builder()
//                .serverId(messageGetServerByServerIdResponse.getServerId())
//                .name(messageGetServerByServerIdResponse.getName())
//                .imgUrl(messageGetServerByServerIdResponse.getImgUrl())
//                .inviteCode(messageGetServerByServerIdResponse.getInviteCode())
//                .profileId(messageGetServerByServerIdResponse.getProfileId())
//                .createdAt(messageGetServerByServerIdResponse.getCreatedAt())
//                .updatedAt(messageGetServerByServerIdResponse.getUpdatedAt())
//                .build());
//    }
//
//    @Override
//    public ResponseObject deleteServerByServerId(DeleteServerRequest request, UserPrincipal currentUser) {
//        MessageDeleteServerByServerIdResponse messageDeleteServerByServerIdResponse = this.serverClientService.deleteServerByServerId(MessageDeleteServerByServerIdRequest.builder()
//                .profileId(currentUser.getUserId())
//                .serverId(request.getServerId())
//                .build());
//        return new ResponseMsg("deleteServerByServerId", HttpStatus.OK, messageDeleteServerByServerIdResponse.isDeleted());
//    }
//
//    @Override
//    public ResponseObject updateServerByServerId(UpdateServerRequest request, UserPrincipal currentUser) {
//        MessageUpdateServerByServerIdResponse messageUpdateServerByServerIdResponse = this.serverClientService.updateServerByServerId(MessageUpdateServerByServerIdRequest.builder()
//                .profileId(currentUser.getUserId())
//                .serverId(request.getServerId())
//                .name(request.getName())
//                .imgUrl(request.getImgUrl())
//                .build());
//        return new ResponseMsg("deleteServerByServerId", HttpStatus.OK, messageUpdateServerByServerIdResponse.getServerId());
//    }
//
//    @Override
//    public ResponseObject getFirstServer(UserPrincipal currentUser) {
//        MessageGetServersByProfileIdResponse messageGetServersByProfileIdResponse = this.serverClientService.getServersByProfileId(MessageGetServersByProfileIdRequest.builder()
//                .profileId(currentUser.getUserId())
//                .page(0)
//                .size(1)
//                .build());
//
//        return new ResponseMsg("getFirstServer", HttpStatus.OK, messageGetServersByProfileIdResponse);
//    }

}
