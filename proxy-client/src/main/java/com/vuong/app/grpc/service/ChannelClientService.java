package com.vuong.app.grpc.service;

import com.vuong.app.business.auth.model.AuthProvider;
import com.vuong.app.exception.wrapper.WTuxException;
import com.vuong.app.grpc.message.auth.GetUserByEmailResponse;
import com.vuong.app.v1.GrpcErrorCode;
import com.vuong.app.v1.GrpcErrorResponse;
import com.vuong.app.v1.GrpcMeta;
import com.vuong.app.v1.discord.*;
import com.vuong.app.v1.user.GrpcGetUserByEmailRequest;
import com.vuong.app.v1.user.GrpcGetUserByEmailResponse;
import com.vuong.app.v1.user.GrpcUser;
import io.grpc.Metadata;
import io.grpc.Status;
import io.grpc.protobuf.ProtoUtils;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
public class ChannelClientService {

    @GrpcClient("grpc-discord-service")
    ChannelServiceGrpc.ChannelServiceBlockingStub channelServiceBlockingStub;

    public GrpcCreateChannelResponse createChannel(GrpcCreateChannelRequest request) {
        try {
            GrpcCreateChannelResponse response = this.channelServiceBlockingStub.createChannel(request);

            return response;

        } catch (Exception ex) {
            Metadata metadata = Status.trailersFromThrowable(ex);
            GrpcErrorResponse errorResponse = metadata.get(ProtoUtils.keyForProto(GrpcErrorResponse.getDefaultInstance()));
            log.error(errorResponse.getErrorCode() + " : " + errorResponse.getMessage());

            throw new WTuxException(errorResponse);
        }
    }

    public GrpcUpdateChannelResponse updateChannel(GrpcUpdateChannelRequest request) {
        try {
            GrpcUpdateChannelResponse response = this.channelServiceBlockingStub.updateChannel(request);

            return response;

        } catch (Exception ex) {
            Metadata metadata = Status.trailersFromThrowable(ex);
            GrpcErrorResponse errorResponse = metadata.get(ProtoUtils.keyForProto(GrpcErrorResponse.getDefaultInstance()));
            log.error(errorResponse.getErrorCode() + " : " + errorResponse.getMessage());

            throw new WTuxException(errorResponse);
        }
    }

    public GrpcDeleteChannelResponse deleteChannel(GrpcDeleteChannelRequest request) {
        try {
            GrpcDeleteChannelResponse response = this.channelServiceBlockingStub.deleteChannel(request);

            return response;

        } catch (Exception ex) {
            Metadata metadata = Status.trailersFromThrowable(ex);
            GrpcErrorResponse errorResponse = metadata.get(ProtoUtils.keyForProto(GrpcErrorResponse.getDefaultInstance()));
            log.error(errorResponse.getErrorCode() + " : " + errorResponse.getMessage());

            throw new WTuxException(errorResponse);
        }
    }

    public Optional<GrpcGetChannelGeneralByServerIdResponse> getChannelGeneralByServerId(GrpcGetChannelGeneralByServerIdRequest request) {
        try {
            GrpcGetChannelGeneralByServerIdResponse response = this.channelServiceBlockingStub.getChannelGeneralByServerId(request);

            return Optional.of(response);

        } catch (Exception ex) {
            Metadata metadata = Status.trailersFromThrowable(ex);
            GrpcErrorResponse errorResponse = metadata.get(ProtoUtils.keyForProto(GrpcErrorResponse.getDefaultInstance()));
            log.error(errorResponse.getErrorCode() + " : " + errorResponse.getMessage());

            if (errorResponse.getErrorCode().getNumber() == GrpcErrorCode.ERROR_CODE_NOT_FOUND_VALUE) {
                return Optional.empty();
            }

            throw new WTuxException(errorResponse);
        }
    }

    public Optional<GrpcGetChannelByChannelIdResponse> getChannelByChannelId(GrpcGetChannelByChannelIdRequest request) {
        try {
            GrpcGetChannelByChannelIdResponse response = this.channelServiceBlockingStub.getChannelByChannelId(request);

            return Optional.of(response);

        } catch (Exception ex) {
            Metadata metadata = Status.trailersFromThrowable(ex);
            GrpcErrorResponse errorResponse = metadata.get(ProtoUtils.keyForProto(GrpcErrorResponse.getDefaultInstance()));
            log.error(errorResponse.getErrorCode() + " : " + errorResponse.getMessage());

            if (errorResponse.getErrorCode().getNumber() == GrpcErrorCode.ERROR_CODE_NOT_FOUND_VALUE) {
                return Optional.empty();
            }

            throw new WTuxException(errorResponse);
        }
    }

    public GrpcGetChannelsByServerIdResponse getChannelsByServerId(GrpcGetChannelsByServerIdRequest request) {
        try {
            GrpcGetChannelsByServerIdResponse response = this.channelServiceBlockingStub.getChannelsByServerId(request);

            return response;

        } catch (Exception ex) {
            Metadata metadata = Status.trailersFromThrowable(ex);
            GrpcErrorResponse errorResponse = metadata.get(ProtoUtils.keyForProto(GrpcErrorResponse.getDefaultInstance()));
            log.error(errorResponse.getErrorCode() + " : " + errorResponse.getMessage());

            throw new WTuxException(errorResponse);
        }
    }

}
