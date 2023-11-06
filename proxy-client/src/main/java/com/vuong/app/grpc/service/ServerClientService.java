package com.vuong.app.grpc.service;

import com.vuong.app.exception.wrapper.WTuxException;
import com.vuong.app.grpc.message.auth.UpdateUserByUserIdResponse;
import com.vuong.app.v1.GrpcErrorCode;
import com.vuong.app.v1.GrpcErrorResponse;
import com.vuong.app.v1.GrpcMeta;
import com.vuong.app.v1.discord.*;
import com.vuong.app.v1.user.GrpcUpdateUserByUserIdRequest;
import com.vuong.app.v1.user.GrpcUpdateUserByUserIdResponse;
import com.vuong.app.v1.user.UserServiceGrpc;
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
import java.util.UUID;

@Service
@Slf4j
public class ServerClientService {

    @GrpcClient("grpc-discord-service")
    ServerServiceGrpc.ServerServiceBlockingStub serverServiceBlockingStub;

    public GrpcCreateServerResponse createServer(GrpcCreateServerRequest request) {
        try {
            GrpcCreateServerResponse response = this.serverServiceBlockingStub.createServer(request);

            return response;

        } catch (Exception ex) {
            Metadata metadata = Status.trailersFromThrowable(ex);
            GrpcErrorResponse errorResponse = metadata.get(ProtoUtils.keyForProto(GrpcErrorResponse.getDefaultInstance()));
            log.error(errorResponse.getErrorCode() + " : " + errorResponse.getMessage());

            throw new WTuxException(errorResponse);
        }
    }

    public GrpcGetServersJoinResponse getServersJoin(GrpcGetServersJoinRequest request) {
        try {
            GrpcGetServersJoinResponse response = this.serverServiceBlockingStub.getServersJoin(request);

            return response;

        } catch (Exception ex) {
            Metadata metadata = Status.trailersFromThrowable(ex);
            GrpcErrorResponse errorResponse = metadata.get(ProtoUtils.keyForProto(GrpcErrorResponse.getDefaultInstance()));
            log.error(errorResponse.getErrorCode() + " : " + errorResponse.getMessage());

            throw new WTuxException(errorResponse);
        }
    }
}
