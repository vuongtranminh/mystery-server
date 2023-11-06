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
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
public class MessageClientService {

    @GrpcClient("grpc-discord-service")
    MessageServiceGrpc.MessageServiceBlockingStub messageServiceBlockingStub;

    public GrpcCreateMessageResponse createMessage(GrpcCreateMessageRequest request) {
        try {
            GrpcCreateMessageResponse response = this.messageServiceBlockingStub.createMessage(request);

            return response;

        } catch (Exception ex) {
            Metadata metadata = Status.trailersFromThrowable(ex);
            GrpcErrorResponse errorResponse = metadata.get(ProtoUtils.keyForProto(GrpcErrorResponse.getDefaultInstance()));
            log.error(errorResponse.getErrorCode() + " : " + errorResponse.getMessage());

            throw new WTuxException(errorResponse);
        }
    }

    public GrpcUpdateMessageResponse updateMessage(GrpcUpdateMessageRequest request) {
        try {
            GrpcUpdateMessageResponse response = this.messageServiceBlockingStub.updateMessage(request);

            return response;

        } catch (Exception ex) {
            Metadata metadata = Status.trailersFromThrowable(ex);
            GrpcErrorResponse errorResponse = metadata.get(ProtoUtils.keyForProto(GrpcErrorResponse.getDefaultInstance()));
            log.error(errorResponse.getErrorCode() + " : " + errorResponse.getMessage());

            throw new WTuxException(errorResponse);
        }
    }

    public GrpcDeleteMessageResponse deleteMessage(GrpcDeleteMessageRequest request) {
        try {
            GrpcDeleteMessageResponse response = this.messageServiceBlockingStub.deleteMessage(request);

            return response;

        } catch (Exception ex) {
            Metadata metadata = Status.trailersFromThrowable(ex);
            GrpcErrorResponse errorResponse = metadata.get(ProtoUtils.keyForProto(GrpcErrorResponse.getDefaultInstance()));
            log.error(errorResponse.getErrorCode() + " : " + errorResponse.getMessage());

            throw new WTuxException(errorResponse);
        }
    }

    public GrpcGetMessagesByChannelIdResponse getMessagesByChannelId(GrpcGetMessagesByChannelIdRequest request) {
        try {
            GrpcGetMessagesByChannelIdResponse response = this.messageServiceBlockingStub.getMessagesByChannelId(request);

            return response;

        } catch (Exception ex) {
            Metadata metadata = Status.trailersFromThrowable(ex);
            GrpcErrorResponse errorResponse = metadata.get(ProtoUtils.keyForProto(GrpcErrorResponse.getDefaultInstance()));
            log.error(errorResponse.getErrorCode() + " : " + errorResponse.getMessage());

            throw new WTuxException(errorResponse);
        }
    }
}
