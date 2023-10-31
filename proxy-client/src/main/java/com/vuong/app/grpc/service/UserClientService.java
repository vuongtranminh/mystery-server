package com.vuong.app.grpc.service;

import com.vuong.app.business.auth.model.AuthProvider;
import com.vuong.app.grpc.message.auth.GetUserByUserIdRequest;
import com.vuong.app.grpc.message.auth.GetUserByUserIdResponse;
import com.vuong.app.v1.*;
import com.vuong.app.v1.user.GrpcGetUserByUserIdRequest;
import com.vuong.app.v1.user.GrpcGetUserByUserIdResponse;
import com.vuong.app.v1.user.GrpcUser;
import com.vuong.app.v1.user.UserServiceGrpc;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserClientService extends BaseClientService {

    @GrpcClient("grpc-user-service")
    UserServiceGrpc.UserServiceBlockingStub userServiceBlockingStub;

    public Optional<GetUserByUserIdResponse> getUserByUserId(GetUserByUserIdRequest request) {
        GrpcRequest req = packRequest(GrpcGetUserByUserIdRequest.newBuilder()
                .setUserId(request.getUserId())
                .build());

        GrpcResponse response = this.userServiceBlockingStub.getUserByUserId(req);

        Optional<GrpcGetUserByUserIdResponse> unpackedResultOptional = unpackedResultQuery(response, GrpcGetUserByUserIdResponse.class);

        if (!unpackedResultOptional.isPresent()) {
            return Optional.empty();
        }

        GrpcGetUserByUserIdResponse unpackedResult = unpackedResultOptional.get();

        GrpcUser grpcUser = unpackedResult.getResult();

        return Optional.of(GetUserByUserIdResponse.builder()
                .userId(grpcUser.getUserId())
                .name(grpcUser.getName())
                .avtUrl(grpcUser.getAvtUrl())
                .bio(grpcUser.getBio())
                .email(grpcUser.getEmail())
                .password(grpcUser.getPassword())
                .verified(grpcUser.getVerified())
                .provider(AuthProvider.forNumber(grpcUser.getProvider().getNumber()))
                .providerId(grpcUser.getProviderId())
                .build());
    }

}
