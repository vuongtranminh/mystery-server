package com.vuong.app.grpc.service;

import com.vuong.app.grpc.message.discord.*;
import com.vuong.app.v1.*;
import com.vuong.app.v1.message.GrpcRequest;
import com.vuong.app.v1.message.GrpcResponse;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ServerClientService extends BaseClientService {

    @GrpcClient("grpc-discord-service")
    ServerServiceGrpc.ServerServiceBlockingStub serverServiceBlockingStub;

    public MessageCreateServerResponse createServer(MessageCreateServerRequest request) {
        GrpcRequest req = packRequest(GrpcCreateServerRequest.newBuilder()
                .setName(request.getName())
                .setImgUrl(request.getImgUrl())
                .setProfileId(request.getProfileId())
                .build());

        GrpcResponse response = this.serverServiceBlockingStub.createServer(req);

        GrpcCreateServerResponse unpackedResult = unpackedResultCommand(response, GrpcCreateServerResponse.class);

        return MessageCreateServerResponse.builder()
                .serverId(unpackedResult.getServerId())
                .build();
    }

    public Optional<MessageGetServerByInviteCodeResponse> getServerByInviteCode(MessageGetServerByInviteCodeRequest request) {
        GrpcRequest req = packRequest(GrpcGetServerByInviteCodeRequest.newBuilder()
                .setInviteCode(request.getInviteCode())
                .build());

        GrpcResponse response = this.serverServiceBlockingStub.getServerByInviteCode(req);

        Optional<GrpcGetServerByInviteCodeResponse> unpackedResultOptional = unpackedResultQuery(response, GrpcGetServerByInviteCodeResponse.class);

        if (!unpackedResultOptional.isPresent()) {
            return Optional.empty();
        }

        GrpcGetServerByInviteCodeResponse unpackedResult = unpackedResultOptional.get();

        GrpcServer grpcServer = unpackedResult.getServer();

        return Optional.of(MessageGetServerByInviteCodeResponse.builder()
                        .serverId(grpcServer.getServerId())
                        .name(grpcServer.getName())
                        .imgUrl(grpcServer.getImgUrl())
                        .inviteCode(grpcServer.getInviteCode())
                        .profileId(grpcServer.getProfileId())
                        .createdAt(Instant.parse(grpcServer.getCreatedAt()))
                        .updatedAt(Instant.parse(grpcServer.getUpdatedAt()))
                .build());
    }

    public Optional<MessageGetServerByServerIdResponse> getServerByServerId(MessageGetServerByServerIdRequest request) {
        GrpcRequest req = packRequest(GrpcGetServerByServerIdRequest.newBuilder()
                .setServerId(request.getServerId())
                .build());

        GrpcResponse response = this.serverServiceBlockingStub.getServerByServerId(req);

        Optional<GrpcGetServerByServerIdResponse> unpackedResultOptional = unpackedResultQuery(response, GrpcGetServerByServerIdResponse.class);

        if (!unpackedResultOptional.isPresent()) {
            return Optional.empty();
        }

        GrpcGetServerByServerIdResponse unpackedResult = unpackedResultOptional.get();

        GrpcServer grpcServer = unpackedResult.getServer();

        return Optional.of(MessageGetServerByServerIdResponse.builder()
                .serverId(grpcServer.getServerId())
                .name(grpcServer.getName())
                .imgUrl(grpcServer.getImgUrl())
                .inviteCode(grpcServer.getInviteCode())
                .profileId(grpcServer.getProfileId())
                .createdAt(Instant.parse(grpcServer.getCreatedAt()))
                .updatedAt(Instant.parse(grpcServer.getUpdatedAt()))
                .build());
    }

    public MessageDeleteServerByServerIdResponse deleteServerByServerId(MessageDeleteServerByServerIdRequest request) {
        GrpcRequest req = packRequest(GrpcDeleteServerByServerIdRequest.newBuilder()
                .setProfileId(request.getProfileId())
                .setServerId(request.getServerId())
                .build());

        GrpcResponse response = this.serverServiceBlockingStub.deleteServerByServerId(req);

        GrpcDeleteServerByServerIdResponse unpackedResult = unpackedResultCommand(response, GrpcDeleteServerByServerIdResponse.class);

        return MessageDeleteServerByServerIdResponse.builder()
                .deleted(unpackedResult.getDeleted())
                .build();
    }

    public MessageUpdateServerByServerIdResponse updateServerByServerId(MessageUpdateServerByServerIdRequest request) {
        GrpcRequest req = packRequest(GrpcUpdateServerByServerIdRequest.newBuilder()
                .setProfileId(request.getProfileId())
                .setServerId(request.getServerId())
                .setName(request.getName())
                .setImgUrl(request.getImgUrl())
                .build());

        GrpcResponse response = this.serverServiceBlockingStub.updateServerByServerId(req);

        GrpcUpdateServerByServerIdResponse unpackedResult = unpackedResultCommand(response, GrpcUpdateServerByServerIdResponse.class);

        return MessageUpdateServerByServerIdResponse.builder()
                .serverId(unpackedResult.getServerId())
                .build();
    }

    public MessageGetServersByProfileIdResponse getServersByProfileId(MessageGetServersByProfileIdRequest request) {
        GrpcRequest req = packRequest(GrpcGetServersByProfileIdRequest.newBuilder()
                .setProfileId(request.getProfileId())
                .setPage(request.getPage())
                .setSize(request.getSize())
                .build());

        GrpcResponse response = this.serverServiceBlockingStub.getServersByProfileId(req);

        GrpcGetServersByProfileIdResponse unpackedResult = unpackedResultCommand(response, GrpcGetServersByProfileIdResponse.class);

        return MessageGetServersByProfileIdResponse.builder()
                .items(unpackedResult.getItemsList().stream().map(grpcServer -> MessageGetServersByProfileIdResponse.MessageServerItem.builder()
                        .serverId(grpcServer.getServerId())
                        .name(grpcServer.getName())
                        .imgUrl(grpcServer.getImgUrl())
                        .inviteCode(grpcServer.getInviteCode())
                        .profileId(grpcServer.getProfileId())
                        .createdAt(Instant.parse(grpcServer.getCreatedAt()))
                        .updatedAt(Instant.parse(grpcServer.getUpdatedAt()))
                        .build()
                ).collect(Collectors.toUnmodifiableList()))
                .totalItems(unpackedResult.getTotalItems())
                .build();
    }
}
