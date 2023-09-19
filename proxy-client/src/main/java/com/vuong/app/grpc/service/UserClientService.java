package com.vuong.app.grpc.service;

import com.google.protobuf.FieldMask;
import com.vuong.app.business.auth.model.UserDto_;
import com.vuong.app.grpc.message.user.GetUserByIdRequest;
import com.vuong.app.grpc.message.user.GetUserByIdResponse;
import com.vuong.app.v1.*;
import com.vuong.app.v1.message.GrpcRequest;
import com.vuong.app.v1.message.GrpcResponse;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Service
public class UserClientService extends BaseClientService {

    @GrpcClient("grpc-user-service")
    UserServiceGrpc.UserServiceBlockingStub synchronousClient;

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

}
