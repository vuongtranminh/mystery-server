package com.vuong.app.business.discord.service.impl;

import com.vuong.app.business.Meta;
import com.vuong.app.business.discord.model.MemberProfile;
import com.vuong.app.business.discord.model.MemberRole;
import com.vuong.app.business.discord.model.payload.GetMemberByServerIdRequest;
import com.vuong.app.business.discord.model.payload.GetMemberByServerIdResponse;
import com.vuong.app.business.discord.model.payload.GetMembersByServerIdRequest;
import com.vuong.app.business.discord.model.payload.GetMembersByServerIdResponse;
import com.vuong.app.business.discord.service.MemberService;
import com.vuong.app.common.api.ResponseMsg;
import com.vuong.app.common.api.ResponseObject;
import com.vuong.app.exception.wrapper.ResourceNotFoundException;
import com.vuong.app.grpc.service.MemberClientService;
import com.vuong.app.security.UserPrincipal;
import com.vuong.app.v1.discord.GrpcGetMemberByServerIdRequest;
import com.vuong.app.v1.discord.GrpcGetMemberByServerIdResponse;
import com.vuong.app.v1.discord.GrpcGetMembersByServerIdRequest;
import com.vuong.app.v1.discord.GrpcGetMembersByServerIdResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class MemberServiceImpl implements MemberService {

    private final MemberClientService memberClientService;

    @Override
    public ResponseObject getMembersByServerId(UserPrincipal currentUser, GetMembersByServerIdRequest request) {
        GrpcGetMembersByServerIdResponse grpcResponse = this.memberClientService.getMembersByServerId(GrpcGetMembersByServerIdRequest.newBuilder()
                .setProfileId(currentUser.getUserId())
                .setServerId(request.getServerId())
                .setPageNumber(request.getPageNumber())
                .setPageSize(request.getPageSize())
                .build());

        return new ResponseMsg("Sign up successfully!", HttpStatus.OK, GetMembersByServerIdResponse.builder()
                .meta(Meta.parse(grpcResponse.getMeta()))
                .content(grpcResponse.getContentList().stream().map(grpcMemberProfile -> MemberProfile.builder()
                        .memberId(grpcMemberProfile.getMemberId())
                        .role(grpcMemberProfile.getRole().getNumber())
                        .serverId(grpcMemberProfile.getServerId())
                        .joinAt(grpcMemberProfile.getJoinAt())
                        .profileId(grpcMemberProfile.getProfileId())
                        .name(grpcMemberProfile.getName())
                        .avtUrl(grpcMemberProfile.getAvtUrl())
                        .build())
                        .collect(Collectors.toUnmodifiableList()))
                .build());
    }

    @Override
    public ResponseObject getMemberByServerId(UserPrincipal currentUser, GetMemberByServerIdRequest request) {
        GrpcGetMemberByServerIdResponse grpcResponse = this.memberClientService.getMemberByServerId(GrpcGetMemberByServerIdRequest.newBuilder()
                .setProfileId(currentUser.getUserId())
                .setServerId(request.getServerId())
                .build())
                .orElseThrow(() -> new ResourceNotFoundException("member", "serverId", request.getServerId()));

        return new ResponseMsg("Sign up successfully!", HttpStatus.OK, GetMemberByServerIdResponse.builder()
                .memberId(grpcResponse.getResult().getMemberId())
                .role(grpcResponse.getResult().getRole().getNumber())
                .serverId(grpcResponse.getResult().getServerId())
                .joinAt(grpcResponse.getResult().getJoinAt())
                .profileId(grpcResponse.getResult().getProfileId())
                .name(grpcResponse.getResult().getName())
                .avtUrl(grpcResponse.getResult().getAvtUrl())
                .build());
    }
}
