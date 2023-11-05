package com.vuong.app.grpc.service;

import com.vuong.app.business.auth.model.AuthProvider;
import com.vuong.app.exception.wrapper.WTuxException;
import com.vuong.app.grpc.message.auth.*;
import com.vuong.app.v1.*;
import com.vuong.app.v1.user.*;
import io.grpc.Metadata;
import io.grpc.Status;
import io.grpc.protobuf.ProtoUtils;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Slf4j
public class UserClientService {

    @GrpcClient("grpc-user-service")
    UserServiceGrpc.UserServiceBlockingStub userServiceBlockingStub;

    public UpdateUserByUserIdResponse updateUserByUserIdRequest(UpdateUserByUserIdRequest request) {
        try {
            GrpcUpdateUserByUserIdResponse response = this.userServiceBlockingStub.updateUserByUserId(GrpcUpdateUserByUserIdRequest.newBuilder()
                    .setUserId(request.getUserId())
                    .setName(request.getName())
                    .setAvtUrl(request.getAvtUrl())
                    .setBio(request.getBio())
                    .build());

            return UpdateUserByUserIdResponse.builder()
                    .userId(response.getUserId())
                    .build();

        } catch (Exception ex) {
            Metadata metadata = Status.trailersFromThrowable(ex);
            GrpcErrorResponse errorResponse = metadata.get(ProtoUtils.keyForProto(GrpcErrorResponse.getDefaultInstance()));
            log.error(errorResponse.getErrorCode() + " : " + errorResponse.getMessage());

            throw new WTuxException(errorResponse);
        }
    }

    public ExistsUserByEmailResponse existsUserByEmail(ExistsUserByEmailRequest request) {
        try {
            GrpcExistsUserByEmailResponse response = this.userServiceBlockingStub.existsUserByEmail(GrpcExistsUserByEmailRequest.newBuilder()
                    .setEmail(request.getEmail())
                    .build());

            return ExistsUserByEmailResponse.builder()
                    .exists(response.getExists())
                    .build();

        } catch (Exception ex) {
            Metadata metadata = Status.trailersFromThrowable(ex);
            GrpcErrorResponse errorResponse = metadata.get(ProtoUtils.keyForProto(GrpcErrorResponse.getDefaultInstance()));
            log.error(errorResponse.getErrorCode() + " : " + errorResponse.getMessage());

            throw new WTuxException(errorResponse);
        }
    }

    public Optional<GetUserByEmailResponse> getUserByEmail(GetUserByEmailRequest request) {
        try {
            GrpcGetUserByEmailResponse response = this.userServiceBlockingStub.getUserByEmail(GrpcGetUserByEmailRequest.newBuilder()
                    .setEmail(request.getEmail())
                    .build());

            GrpcUser grpcUser = response.getResult();

            return Optional.of(GetUserByEmailResponse.builder()
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

    public Optional<GetUserByUserIdResponse> getUserByUserId(GetUserByUserIdRequest request) {
        try {
            GrpcGetUserByUserIdResponse response = this.userServiceBlockingStub.getUserByUserId(GrpcGetUserByUserIdRequest.newBuilder()
                    .setUserId(request.getUserId())
                    .build());

            GrpcUser grpcUser = response.getResult();

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

}
