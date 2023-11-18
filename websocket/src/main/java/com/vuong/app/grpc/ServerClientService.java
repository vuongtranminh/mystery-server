package com.vuong.app.grpc;

import com.vuong.app.v1.GrpcErrorResponse;
import com.vuong.app.v1.discord.*;
import io.grpc.Metadata;
import io.grpc.Status;
import io.grpc.protobuf.ProtoUtils;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ServerClientService {

    @GrpcClient("grpc-discord-service")
    ServerServiceGrpc.ServerServiceBlockingStub serverServiceBlockingStub;

    public GrpcGetServerJoinIdsResponse getServerJoinIds(GrpcGetServerJoinIdsRequest request) {
        try {
            GrpcGetServerJoinIdsResponse response = this.serverServiceBlockingStub.getServerJoinIds(request);

            return response;

        } catch (Exception ex) {
            Metadata metadata = Status.trailersFromThrowable(ex);
            GrpcErrorResponse errorResponse = metadata.get(ProtoUtils.keyForProto(GrpcErrorResponse.getDefaultInstance()));
            log.error(errorResponse.getErrorCode() + " : " + errorResponse.getMessage());

            return null;
        }
    }
}
