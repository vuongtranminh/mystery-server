//package com.vuong.app.business.discord.service.impl;
//
//import com.vuong.app.business.discord.model.payload.CreateServerRequest;
//import com.vuong.app.business.discord.model.payload.DeleteServerRequest;
//import com.vuong.app.business.discord.model.payload.GetServerResponse;
//import com.vuong.app.business.discord.model.payload.UpdateServerRequest;
//import com.vuong.app.business.discord.service.ServerService;
//import com.vuong.app.common.api.ResponseMsg;
//import com.vuong.app.common.api.ResponseObject;
//import com.vuong.app.exception.wrapper.ResourceNotFoundException;
//import com.vuong.app.grpc.message.discord.*;
//import com.vuong.app.grpc.service.ServerClientService;
//import com.vuong.app.security.UserPrincipal;
//import lombok.RequiredArgsConstructor;
//import org.springframework.http.HttpStatus;
//import org.springframework.stereotype.Service;
//
//@Service
//@RequiredArgsConstructor
//public class ServerServiceImpl implements ServerService {
//
//    private final ServerClientService serverClientService;
//
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
//
//}
