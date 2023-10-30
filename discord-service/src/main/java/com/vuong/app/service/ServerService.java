package com.vuong.app.service;

import com.vuong.app.doman.*;
import com.vuong.app.dto.member.MemberFilterParameter;
import com.vuong.app.dto.member.MemberSortParameter;
import com.vuong.app.dto.server.ServerFilterParameter;
import com.vuong.app.dto.server.ServerSortParameter;
import com.vuong.app.jpa.query.QueryBuilder;
import com.vuong.app.jpa.query.QueryHelper;
import com.vuong.app.jpa.query.ServiceHelper;
import com.vuong.app.operator.DateOperators;
import com.vuong.app.operator.NumberOperators;
import com.vuong.app.operator.StringOperators;
import com.vuong.app.repository.MemberRepository;
import com.vuong.app.repository.ServerRepository;
import com.vuong.app.v1.*;
import com.vuong.app.v1.message.GrpcErrorCode;
import com.vuong.app.v1.message.GrpcRequest;
import com.vuong.app.v1.message.GrpcResponse;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.Fetch;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.transaction.Transactional;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@GrpcService
@Transactional
@Slf4j
@RequiredArgsConstructor
public class ServerService extends ServerServiceGrpc.ServerServiceImplBase {

    private final ServerRepository serverRepository;
    private final MemberService memberService;

    @Override
    public void createServer(GrpcRequest request, StreamObserver<GrpcResponse> responseObserver) {
        GrpcCreateServerRequest req = ServiceHelper.unpackedRequest(request, GrpcCreateServerRequest.class);

        Channel channel = Channel.builder()
                .name("general")
                .profileId(req.getProfileId())
                .type(ChannelType.TEXT)
                .build();

        Member member = Member.builder()
                .profileId(req.getProfileId())
                .memberRole(MemberRole.ADMIN)
                .build();

        Server server = Server.builder()
                .name(req.getName())
                .imgUrl(req.getImgUrl())
                .profileId(req.getProfileId())
                .inviteCode(UUID.randomUUID().toString())
                .build();

        server.addChanel(channel);
        server.addMember(member);

        server = this.serverRepository.save(server);

        GrpcCreateServerResponse response = GrpcCreateServerResponse.newBuilder().setServerId(server.getServerId()).build();

        ServiceHelper.next(responseObserver, ServiceHelper.packedSuccessResponse(response));
    }

    @Override
    public void getServerByInviteCode(GrpcRequest request, StreamObserver<GrpcResponse> responseObserver) {
        GrpcGetServerByInviteCodeRequest req = ServiceHelper.unpackedRequest(request, GrpcGetServerByInviteCodeRequest.class);

        Optional<Server> serverOptional = this.serverRepository.findByInviteCode(req.getInviteCode());

        if (!serverOptional.isPresent()) {
            ServiceHelper.next(responseObserver, ServiceHelper.packedErrorResponse(GrpcErrorCode.ERROR_CODE_NOT_FOUND, "server not found with id"));
            return;
        }

        Server server = serverOptional.get();

        GrpcGetServerByInviteCodeResponse response = GrpcGetServerByInviteCodeResponse.newBuilder()
                .setServer(GrpcServer.newBuilder()
                        .setServerId(server.getServerId())
                        .setName(server.getName())
                        .setImgUrl(server.getImgUrl())
                        .setInviteCode(server.getInviteCode())
                        .setProfileId(server.getProfileId())
                        .setCreatedAt(server.getCreatedAt().toString())
                        .setUpdatedAt(server.getUpdatedAt().toString())
                        .build())
                .build();

        ServiceHelper.next(responseObserver, ServiceHelper.packedSuccessResponse(response));
    }

    @Override
    public void getServerByServerId(GrpcRequest request, StreamObserver<GrpcResponse> responseObserver) {
        GrpcGetServerByServerIdRequest req = ServiceHelper.unpackedRequest(request, GrpcGetServerByServerIdRequest.class);

        Optional<Server> serverOptional = this.serverRepository.findById(req.getServerId());

        if (!serverOptional.isPresent()) {
            ServiceHelper.next(responseObserver, ServiceHelper.packedErrorResponse(GrpcErrorCode.ERROR_CODE_NOT_FOUND, "server not found with id"));
            return;
        }

        Server server = serverOptional.get();

        GrpcGetServerByServerIdResponse response = GrpcGetServerByServerIdResponse.newBuilder()
                .setServer(GrpcServer.newBuilder()
                        .setServerId(server.getServerId())
                        .setName(server.getName())
                        .setImgUrl(server.getImgUrl())
                        .setInviteCode(server.getInviteCode())
                        .setProfileId(server.getProfileId())
                        .setCreatedAt(server.getCreatedAt().toString())
                        .setUpdatedAt(server.getUpdatedAt().toString())
                        .build())
                .build();

        ServiceHelper.next(responseObserver, ServiceHelper.packedSuccessResponse(response));
    }

    @Override
    public void deleteServerByServerId(GrpcRequest request, StreamObserver<GrpcResponse> responseObserver) {
        GrpcDeleteServerByServerIdRequest req = ServiceHelper.unpackedRequest(request, GrpcDeleteServerByServerIdRequest.class);

        Optional<Server> serverDeleteOptional = this.serverRepository.findById(req.getServerId());

        GrpcDeleteServerByServerIdResponse.Builder builderResponse = GrpcDeleteServerByServerIdResponse.newBuilder();

        if (!serverDeleteOptional.isPresent()) {
            builderResponse.setDeleted(true);
            ServiceHelper.next(responseObserver, ServiceHelper.packedSuccessResponse(builderResponse.build()));
            return;
        }

        Server serverDelete = serverDeleteOptional.get();
        if (serverDelete.getProfileId() != req.getProfileId()) {
            // permisstion Không có quyền
            ServiceHelper.next(responseObserver, ServiceHelper.packedErrorResponse(GrpcErrorCode.ERROR_CODE_NOT_FOUND, "user not found with id"));
            return;
        }

        this.serverRepository.deleteById(serverDelete.getServerId());

        builderResponse.setDeleted(true);
        ServiceHelper.next(responseObserver, ServiceHelper.packedSuccessResponse(builderResponse.build()));
    }

    @Override
    public void updateServerByServerId(GrpcRequest request, StreamObserver<GrpcResponse> responseObserver) {
        GrpcUpdateServerByServerIdRequest req = ServiceHelper.unpackedRequest(request, GrpcUpdateServerByServerIdRequest.class);

        Optional<Server> serverUpdateOptional = this.serverRepository.findById(req.getServerId());

        if (!serverUpdateOptional.isPresent()) {
            ServiceHelper.next(responseObserver, ServiceHelper.packedErrorResponse(GrpcErrorCode.ERROR_CODE_NOT_FOUND, "user not found with id"));
            return;
        }

        Server serverUpdate = serverUpdateOptional.get();
        if (serverUpdate.getProfileId() != req.getProfileId()) {
            // permisstion Không có quyền
            ServiceHelper.next(responseObserver, ServiceHelper.packedErrorResponse(GrpcErrorCode.ERROR_CODE_NOT_FOUND, "user not found with id"));
            return;
        }
        serverUpdate.setName(req.getName());
        serverUpdate.setImgUrl(req.getImgUrl());

        Server server = this.serverRepository.save(serverUpdate);

        GrpcUpdateServerByServerIdResponse response = GrpcUpdateServerByServerIdResponse.newBuilder().setServerId(server.getServerId()).build();

        ServiceHelper.next(responseObserver, ServiceHelper.packedSuccessResponse(response));
    }

    @Override
    public void getServersByProfileId(GrpcRequest request, StreamObserver<GrpcResponse> responseObserver) {
        GrpcGetServersByProfileIdRequest req = ServiceHelper.unpackedRequest(request, GrpcGetServersByProfileIdRequest.class);

        Page<MemberRepository.MemberWithServer> memberPage = this.memberService.getMembersByProfileId(req.getProfileId(), req.getPage(), req.getSize());

        GrpcGetServersByProfileIdResponse response = GrpcGetServersByProfileIdResponse.newBuilder()
                .addAllItems(memberPage.getContent().stream().map(member -> {
                    Server server = member.getServer();
                    return GrpcServer.newBuilder()
                            .setServerId(server.getServerId())
                            .setName(server.getName())
                            .setImgUrl(server.getImgUrl())
                            .setInviteCode(server.getInviteCode())
                            .setProfileId(server.getProfileId())
                            .setCreatedAt(server.getCreatedAt().toString())
                            .setUpdatedAt(server.getUpdatedAt().toString())
                            .build();
                }).collect(Collectors.toUnmodifiableList()))
                .setTotalItems(memberPage.getTotalPages())
                .build();

        ServiceHelper.next(responseObserver, ServiceHelper.packedSuccessResponse(response));
    }

    private void buildSortOrder(QueryBuilder queryBuilder, MemberSortParameter memberSortParameter, ServerSortParameter serverSortParameter) {
        if (memberSortParameter != null) {
            QueryHelper.buildOneSortOrder(queryBuilder, memberSortParameter.getCreatedAt(), Member_.CREATED_AT);
            QueryHelper.buildOneSortOrder(queryBuilder, memberSortParameter.getUpdatedAt(), Member_.UPDATED_AT);
        }

        if (serverSortParameter != null) {
            QueryHelper.buildOneSortOrder(queryBuilder, serverSortParameter.getCreatedAt(), Server_.CREATED_AT);
            QueryHelper.buildOneSortOrder(queryBuilder, serverSortParameter.getUpdatedAt(), Server_.UPDATED_AT);
        }
    }

    private void buildFilter(QueryBuilder queryBuilder, MemberFilterParameter memberFilterParameter, ServerFilterParameter serverFilterParameter) {
        if (memberFilterParameter != null) {
            QueryHelper.buildOneNumberOperatorFilter(queryBuilder, memberFilterParameter.getMemberId(), Member_.MEMBER_ID);
            QueryHelper.buildOneNumberOperatorFilter(queryBuilder, memberFilterParameter.getProfileId(), Member_.PROFILE_ID);
            QueryHelper.buildOneNumberOperatorFilter(queryBuilder, memberFilterParameter.getServerId(), Server_.SERVER_ID);
            QueryHelper.buildOneListOperatorFilter(queryBuilder, memberFilterParameter.getMemberRoles(), Member_.MEMBER_ROLE);
            QueryHelper.buildOneDateOperatorFilter(queryBuilder, memberFilterParameter.getCreatedAt(), Member_.CREATED_AT);
            QueryHelper.buildOneDateOperatorFilter(queryBuilder, memberFilterParameter.getUpdatedAt(), Member_.UPDATED_AT);
        }

        if (serverFilterParameter != null) {
            QueryHelper.buildOneNumberOperatorFilter(queryBuilder, serverFilterParameter.getServerId(), Server_.SERVER_ID);
            QueryHelper.buildOneStringOperatorFilter(queryBuilder, serverFilterParameter.getName(), Server_.NAME);
            QueryHelper.buildOneStringOperatorFilter(queryBuilder, serverFilterParameter.getInviteCode(), Server_.INVITE_CODE);
            QueryHelper.buildOneNumberOperatorFilter(queryBuilder, serverFilterParameter.getProfileId(), Server_.PROFILE_ID);
            QueryHelper.buildOneDateOperatorFilter(queryBuilder, serverFilterParameter.getCreatedAt(), Server_.CREATED_AT);
            QueryHelper.buildOneDateOperatorFilter(queryBuilder, serverFilterParameter.getUpdatedAt(), Server_.UPDATED_AT);
        }
    }

    private Specification<Member> fetchServer() {
        return (root, query, criteriaBuilder) -> {
//            Fetch<Member, Server> f = root.fetch(Member_.SERVER, JoinType.INNER);
//            Join<Member, Server> join = (Join<Member, Server>) f;
            Fetch<Member, Server> f = root.fetch(Member_.SERVER, JoinType.INNER);
            Join<Member, Server> join = (Join<Member, Server>) f;
            return join.getOn();
        };
    }
}
