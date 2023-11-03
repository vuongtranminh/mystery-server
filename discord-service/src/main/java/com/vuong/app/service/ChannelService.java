package com.vuong.app.service;

import com.vuong.app.doman.*;
import com.vuong.app.repository.ChannelRepository;
import com.vuong.app.repository.MemberRepository;
import com.vuong.app.repository.ServerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;

import java.util.Optional;

@GrpcService
@Slf4j
@RequiredArgsConstructor
public class ChannelService extends ChannelServiceGrpc.ChannelServiceImplBase {
    
    private final ChannelRepository channelRepository;
    private final MemberRepository memberRepository;
    private final ServerRepository serverRepository;

    @Override
    public void getChannelByChannelId(GrpcRequest request, StreamObserver<GrpcResponse> responseObserver) {
        GrpcGetChannelByChannelIdRequest req = ServiceHelper.unpackedRequest(request, GrpcGetChannelByChannelIdRequest.class);

        Optional<Channel> channelOptional = this.channelRepository.findById(req.getChannelId());

        if (!channelOptional.isPresent()) {
            ServiceHelper.next(responseObserver, ServiceHelper.packedErrorResponse(GrpcErrorCode.ERROR_CODE_NOT_FOUND, "server not found with id"));
            return;
        }

        Channel channel = channelOptional.get();

        GrpcGetChannelByChannelIdResponse response = GrpcGetChannelByChannelIdResponse.newBuilder()
                .setChannel(GrpcChannel.newBuilder()
                        .setChannelId(channel.getChannelId())
                        .setName(channel.getName())
                        .setType(GrpcChannelType.forNumber(channel.getType().getNumber()))
                        .setProfileId(channel.getProfileId())
                        .setServerId(channel.getServer().getServerId())
                        .setCreatedAt(channel.getCreatedAt().toString())
                        .setUpdatedAt(channel.getUpdatedAt().toString())
                        .build())
                .build();

        ServiceHelper.next(responseObserver, ServiceHelper.packedSuccessResponse(response));
    }

    @Override
    public void createChannel(GrpcRequest request, StreamObserver<GrpcResponse> responseObserver) {
        GrpcCreateChannelRequest req = ServiceHelper.unpackedRequest(request, GrpcCreateChannelRequest.class);

        if (req.getName().equals("general")) {
            ServiceHelper.next(responseObserver, ServiceHelper.packedErrorResponse(GrpcErrorCode.ERROR_CODE_NOT_FOUND, "Name cannot be 'general'"));
            return;
        }

        Optional<Server> serverOptional = this.serverRepository.findById(req.getServerId());

        if (!serverOptional.isPresent()) {
            ServiceHelper.next(responseObserver, ServiceHelper.packedErrorResponse(GrpcErrorCode.ERROR_CODE_NOT_FOUND, "Server not found"));
            return;
        }

        Server server = serverOptional.get();

        Optional<Member> memberOptional = this.memberRepository.findByProfileIdAndAndServer(req.getProfileId(), server);

        if (!memberOptional.isPresent()) {
            ServiceHelper.next(responseObserver, ServiceHelper.packedErrorResponse(GrpcErrorCode.ERROR_CODE_NOT_FOUND, "Member not found"));
            return;
        }

        Member member = memberOptional.get();

        boolean isAuthUpdate = member.getMemberRole().getNumber() == MemberRole.ADMIN.getNumber() || member.getMemberRole().getNumber() == MemberRole.MODERATOR.getNumber();

        if (!isAuthUpdate) { // permistion
            ServiceHelper.next(responseObserver, ServiceHelper.packedErrorResponse(GrpcErrorCode.ERROR_CODE_NOT_FOUND, "permistion not found"));
            return;
        }

        Channel channel = Channel.builder()
                .name(req.getName())
                .profileId(req.getProfileId())
                .type(ChannelType.forNumber(req.getType().getNumber()))
                .server(server)
                .build();

        channel = this.channelRepository.save(channel);

        GrpcCreateChannelResponse response = GrpcCreateChannelResponse.newBuilder().setChannelId(channel.getChannelId()).build();

        ServiceHelper.next(responseObserver, ServiceHelper.packedSuccessResponse(response));
    }

    @Override
    public void deleteChannelByChannelId(GrpcRequest request, StreamObserver<GrpcResponse> responseObserver) {
        GrpcDeleteChannelByChannelIdRequest req = ServiceHelper.unpackedRequest(request, GrpcDeleteChannelByChannelIdRequest.class);

        Optional<Channel> channelOptional = this.channelRepository.findById(req.getChannelId());

        if (!channelOptional.isPresent()) {
            ServiceHelper.next(responseObserver, ServiceHelper.packedErrorResponse(GrpcErrorCode.ERROR_CODE_NOT_FOUND, "Channel not found with id"));
            return;
        }

        Channel channel = channelOptional.get();

        if (channel.getName().equals("general")) {
            ServiceHelper.next(responseObserver, ServiceHelper.packedErrorResponse(GrpcErrorCode.ERROR_CODE_NOT_FOUND, "Name cannot be 'general'"));
            return;
        }

        Server server = channel.getServer();

        Optional<Member> memberOptional = this.memberRepository.findByProfileIdAndAndServer(req.getProfileId(), server);

        if (!memberOptional.isPresent()) {
            ServiceHelper.next(responseObserver, ServiceHelper.packedErrorResponse(GrpcErrorCode.ERROR_CODE_NOT_FOUND, "Member not found"));
            return;
        }

        Member member = memberOptional.get();

        boolean isAuthUpdate = member.getMemberRole().getNumber() == MemberRole.ADMIN.getNumber() || member.getMemberRole().getNumber() == MemberRole.MODERATOR.getNumber();

        if (!isAuthUpdate) { // permistion
            ServiceHelper.next(responseObserver, ServiceHelper.packedErrorResponse(GrpcErrorCode.ERROR_CODE_NOT_FOUND, "permistion not found"));
            return;
        }

        this.channelRepository.deleteById(channel.getChannelId());

        GrpcDeleteChannelByChannelIdResponse response = GrpcDeleteChannelByChannelIdResponse.newBuilder().setDeleted(true).build();

        ServiceHelper.next(responseObserver, ServiceHelper.packedSuccessResponse(response));
    }

    @Override
    public void updateChannelByChannelId(GrpcRequest request, StreamObserver<GrpcResponse> responseObserver) {
        GrpcUpdateChannelByChannelIdRequest req = ServiceHelper.unpackedRequest(request, GrpcUpdateChannelByChannelIdRequest.class);

        Optional<Channel> channelOptional = this.channelRepository.findById(req.getChannelId());

        if (!channelOptional.isPresent()) {
            ServiceHelper.next(responseObserver, ServiceHelper.packedErrorResponse(GrpcErrorCode.ERROR_CODE_NOT_FOUND, "Channel not found with id"));
            return;
        }

        Channel channel = channelOptional.get();

        if (channel.getName().equals("general")) {
            ServiceHelper.next(responseObserver, ServiceHelper.packedErrorResponse(GrpcErrorCode.ERROR_CODE_NOT_FOUND, "Name cannot be 'general'"));
            return;
        }

        Server server = channel.getServer();

        Optional<Member> memberOptional = this.memberRepository.findByProfileIdAndAndServer(req.getProfileId(), server);

        if (!memberOptional.isPresent()) {
            ServiceHelper.next(responseObserver, ServiceHelper.packedErrorResponse(GrpcErrorCode.ERROR_CODE_NOT_FOUND, "Member not found"));
            return;
        }

        Member member = memberOptional.get();

        boolean isAuthUpdate = member.getMemberRole().getNumber() == MemberRole.ADMIN.getNumber() || member.getMemberRole().getNumber() == MemberRole.MODERATOR.getNumber();

        if (!isAuthUpdate) { // permistion
            ServiceHelper.next(responseObserver, ServiceHelper.packedErrorResponse(GrpcErrorCode.ERROR_CODE_NOT_FOUND, "permistion not found"));
            return;
        }

        channel.setName(req.getName());
        channel.setType(ChannelType.forNumber(req.getType().getNumber()));

        channel = this.channelRepository.save(channel);

        GrpcUpdateChannelByChannelIdResponse response = GrpcUpdateChannelByChannelIdResponse.newBuilder().setChannelId(channel.getChannelId()).build();

        ServiceHelper.next(responseObserver, ServiceHelper.packedSuccessResponse(response));
    }
}
