package com.vuong.app.grpc.service;

import com.google.protobuf.FieldMask;
import com.vuong.app.business.auth.model.*;
import com.vuong.app.grpc.message.auth.*;
import com.vuong.app.grpc.service.BaseClientService;
import com.vuong.app.v1.*;
import com.vuong.app.v1.message.GrpcRequest;
import com.vuong.app.v1.message.GrpcResponse;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Service
public class AuthClientService extends BaseClientService {

    @GrpcClient("grpc-user-service")
    UserServiceGrpc.UserServiceBlockingStub synchronousClient;

    public Optional<GetUserByEmailResponse> getUserByEmail(GetUserByEmailRequest request) {
        Set<String> requestedFields = new HashSet<>();
        requestedFields.add(UserDto_.USER_ID);
        requestedFields.add(UserDto_.NAME);
        requestedFields.add(UserDto_.AVATAR);
        requestedFields.add(UserDto_.BIO);
        requestedFields.add(UserDto_.EMAIL);
        requestedFields.add(UserDto_.PASSWORD);
        requestedFields.add(UserDto_.PROVIDER);
        requestedFields.add(UserDto_.PROVIDER_ID);

        GrpcRequest req = packRequest(GrpcGetUserByEmailRequest.newBuilder()
                .setEmail(request.getEmail())
                .setFieldMask(FieldMask.newBuilder()
                        .addAllPaths(requestedFields)
                        .build())
                .build());

        GrpcResponse response = this.synchronousClient.grpcFindByEmail(req);

        Optional<GrpcGetUserByEmailResponse> unpackedResultOptional = unpackedResult(response, GrpcGetUserByEmailResponse.class);

        if (!unpackedResultOptional.isPresent()) {
            return Optional.empty();
        }

        GrpcGetUserByEmailResponse unpackedResult = unpackedResultOptional.get();

        GrpcUserDto grpcUserDto = unpackedResult.getUser();

        return Optional.of(GetUserByEmailResponse.builder()
                .userId(grpcUserDto.getUserId())
                .name(grpcUserDto.getName())
                .avatar(grpcUserDto.getAvatar())
                .bio(grpcUserDto.getBio())
                .email(grpcUserDto.getEmail())
                .password(grpcUserDto.getPassword())
                .provider(AuthProvider.forNumber(grpcUserDto.getProvider().getNumber()))
                .providerId(grpcUserDto.getProviderId())
                .build());
    }

    public Optional<GetUserByIdResponse> getUserById(GetUserByIdRequest request) {
        Set<String> requestedFields = new HashSet<>();
        requestedFields.add(UserDto_.USER_ID);
        requestedFields.add(UserDto_.NAME);
        requestedFields.add(UserDto_.AVATAR);
        requestedFields.add(UserDto_.BIO);
        requestedFields.add(UserDto_.EMAIL);
        requestedFields.add(UserDto_.PASSWORD);
        requestedFields.add(UserDto_.PROVIDER);
        requestedFields.add(UserDto_.PROVIDER_ID);

        GrpcRequest req = packRequest(GrpcGetUserByIdRequest.newBuilder()
                .setUserId(request.getUserId())
                .setFieldMask(FieldMask.newBuilder()
                        .addAllPaths(requestedFields)
                        .build())
                .build());

        GrpcResponse response = this.synchronousClient.grpcFindById(req);

        Optional<GrpcGetUserByIdResponse> unpackedResultOptional = unpackedResult(response, GrpcGetUserByIdResponse.class);

        if (!unpackedResultOptional.isPresent()) {
            return Optional.empty();
        }

        GrpcGetUserByIdResponse unpackedResult = unpackedResultOptional.get();

        return Optional.of(GetUserByIdResponse.builder()
                .userId(unpackedResult.getUser().getUserId())
                .name(unpackedResult.getUser().getName())
                .avatar(unpackedResult.getUser().getAvatar())
                .bio(unpackedResult.getUser().getBio())
                .email(unpackedResult.getUser().getEmail())
                .password(unpackedResult.getUser().getPassword())
                .build());
    }

    public Optional<CreateUserResponse> create(CreateUserRequest request) {
        GrpcRequest req = packRequest(GrpcCreateUserRequest.newBuilder()
                .setName(request.getName())
                .setAvatar(request.getAvatar())
                .setEmail(request.getEmail())
                .setPassword(request.getPassword())
                .setProvider(GrpcAuthProvider.forNumber(request.getProvider().getNumber()))
                .setProviderId(request.getProviderId())
                .build());

        GrpcResponse response = this.synchronousClient.grpcCreate(req);

        Optional<GrpcCreateUserResponse> unpackedResultOptional = unpackedResult(response, GrpcCreateUserResponse.class);

        if (!unpackedResultOptional.isPresent()) {
            return Optional.empty();
        }

        GrpcCreateUserResponse unpackedResult = unpackedResultOptional.get();

        return Optional.of(CreateUserResponse.builder()
                .userId(unpackedResult.getUserId())
                .build());
    }

    public Optional<UpdateUserResponse> update(UpdateUserRequest request) {
        Set<String> requestedFields = new HashSet<>();
        requestedFields.add(UserDto_.NAME);
        requestedFields.add(UserDto_.AVATAR);

        GrpcRequest req = packRequest(GrpcUpdateUserRequest.newBuilder()
                .setUserId(request.getUserId())
                .setUpdate(GrpcUserUpdateOperation.newBuilder()
                        .setName(request.getName())
                        .setAvatar(request.getAvatar())
                        .build())
                .setUpdateMask(FieldMask.newBuilder()
                        .addAllPaths(requestedFields)
                        .build())
                .build());

        GrpcResponse response = this.synchronousClient.grpcUpdate(req);

        Optional<GrpcUpdateUserResponse> unpackedResultOptional = unpackedResult(response, GrpcUpdateUserResponse.class);

        if (!unpackedResultOptional.isPresent()) {
            return Optional.empty();
        }

        GrpcUpdateUserResponse unpackedResult = unpackedResultOptional.get();

        return Optional.of(UpdateUserResponse.builder()
                .userId(unpackedResult.getUserId())
                .build());
    }

    public boolean existsByEmail(String email) {
        GrpcRequest req = packRequest(GrpcExistsByEmailRequest.newBuilder()
                .setEmail(email)
                .build());

        GrpcResponse response = this.synchronousClient.grpcExistsByEmail(req);

        Optional<GrpcExistsByEmailResponse> unpackedResultOptional = unpackedResult(response, GrpcExistsByEmailResponse.class);

        if (!unpackedResultOptional.isPresent()) {
            return false;
        }

        GrpcExistsByEmailResponse unpackedResult = unpackedResultOptional.get();

        return unpackedResult.getExists();
    }

    public Optional<CreateRefreshTokenResponse> createRefreshToken(CreateRefreshTokenRequest request) {
        return Optional.empty();
    }
}
