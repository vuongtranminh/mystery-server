package com.vuong.app.service;

import com.vuong.app.doman.Channel;
import com.vuong.app.doman.Member;
import com.vuong.app.doman.MemberRole;
import com.vuong.app.doman.Server;
import com.vuong.app.jpa.query.ServiceHelper;
import com.vuong.app.repository.ServerRepository;
import com.vuong.app.v1.*;
import com.vuong.app.v1.message.GrpcErrorCode;
import com.vuong.app.v1.message.GrpcRequest;
import com.vuong.app.v1.message.GrpcResponse;
import io.grpc.ServerRegistry;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;

import javax.transaction.Transactional;
import java.util.Optional;
import java.util.UUID;

@GrpcService
@Transactional
@Slf4j
@RequiredArgsConstructor
public class ServerService extends ServerServiceGrpc.ServerServiceImplBase {

    private final ServerRepository serverRepository;

    @Override
    public void createServer(GrpcRequest request, StreamObserver<GrpcResponse> responseObserver) {
        GrpcCreateServerRequest req = ServiceHelper.unpackedRequest(request, GrpcCreateServerRequest.class);

        Channel channel = Channel.builder()
                .name("general")
                .profileId(req.getProfileId())
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

}
