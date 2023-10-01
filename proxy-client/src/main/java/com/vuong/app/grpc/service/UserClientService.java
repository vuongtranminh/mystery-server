package com.vuong.app.grpc.service;

import com.vuong.app.business.auth.model.AuthProvider;
import com.vuong.app.grpc.message.auth.GetUserByUserIdRequest;
import com.vuong.app.grpc.message.auth.GetUserByUserIdResponse;
import com.vuong.app.v1.*;
import com.vuong.app.v1.message.GrpcRequest;
import com.vuong.app.v1.message.GrpcResponse;
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

        GrpcUser grpcUser = unpackedResult.getUser();

        return Optional.of(GetUserByUserIdResponse.builder()
                .userId(grpcUser.getUserId())
                .name(grpcUser.getName())
                .avatar(grpcUser.getAvatar())
                .bio(grpcUser.getBio())
                .email(grpcUser.getEmail())
                .password(grpcUser.getPassword())
                .verified(grpcUser.getVerified())
                .provider(AuthProvider.forNumber(grpcUser.getProvider().getNumber()))
                .providerId(grpcUser.getProviderId())
                .build());
    }

//    public UserList getUsers(UserListOptions options, DataFetchingEnvironment env) {
//
//        GrpcUserListOptionsRequest userListOptionsRequest = UserMappingHelper.map(options, env);
//
//        GrpcRequest req = packRequest(userListOptionsRequest);
//
//        GrpcResponse response = this.synchronousClient.grpcFindAll(req);
//
//        Optional<GrpcUserPaginatedResponse> unpackedResultOptional = unpackedResult(response, GrpcUserPaginatedResponse.class);
//
//        if (!unpackedResultOptional.isPresent()) {
//            return null;
//        }
//
//        GrpcUserPaginatedResponse unpackedResult = unpackedResultOptional.get();
//
//        return UserList.builder()
//                .items(unpackedResult.getItemsList().stream().map(userResponse -> UserDto.builder()
//                        .id(userResponse.getUserId())
//                        .name(userResponse.getName())
//                        .avatar(userResponse.getAvatar())
//                        .bio(userResponse.getBio())
//                        .build()).collect(Collectors.toUnmodifiableList()))
//                .totalItems(unpackedResult.getTotalItems())
//                .build();
//    }

//    public Optional<GetUserByIdResponse> getUserById(GetUserByIdRequest request) {
//        Set<String> requestedFields = new HashSet<>();
//        requestedFields.add(UserDto_.USER_ID);
//        requestedFields.add(UserDto_.NAME);
//        requestedFields.add(UserDto_.AVATAR);
//        requestedFields.add(UserDto_.BIO);
//        requestedFields.add(UserDto_.EMAIL);
//        requestedFields.add(UserDto_.PASSWORD);
//        requestedFields.add(UserDto_.PROVIDER);
//        requestedFields.add(UserDto_.PROVIDER_ID);
//
//        GrpcRequest req = packRequest(GrpcGetUserByIdRequest.newBuilder()
//                .setUserId(request.getUserId())
//                .setFieldMask(FieldMask.newBuilder()
//                        .addAllPaths(requestedFields)
//                        .build())
//                .build());
//
//        GrpcResponse response = this.synchronousClient.grpcFindById(req);
//
//        Optional<GrpcGetUserByIdResponse> unpackedResultOptional = unpackedResultQuery(response, GrpcGetUserByIdResponse.class);
//
//        if (!unpackedResultOptional.isPresent()) {
//            return Optional.empty();
//        }
//
//        GrpcGetUserByIdResponse unpackedResult = unpackedResultOptional.get();
//
//        return Optional.of(GetUserByIdResponse.builder()
//                .userId(unpackedResult.getUser().getUserId())
//                .name(unpackedResult.getUser().getName())
//                .avatar(unpackedResult.getUser().getAvatar())
//                .bio(unpackedResult.getUser().getBio())
//                .email(unpackedResult.getUser().getEmail())
//                .password(unpackedResult.getUser().getPassword())
//                .build());
//    }

}
