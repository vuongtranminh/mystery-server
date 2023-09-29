package com.vuong.app.grpc.service;

import com.vuong.app.business.auth.model.*;
import com.vuong.app.grpc.message.auth.*;
import com.vuong.app.v1.*;
import com.vuong.app.v1.message.GrpcRequest;
import com.vuong.app.v1.message.GrpcResponse;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Date;
import java.util.Optional;

@Service
public class AuthClientService extends BaseClientService {

    @GrpcClient("grpc-user-service")
    UserServiceGrpc.UserServiceBlockingStub userServiceBlockingStub;

    @GrpcClient("grpc-user-service")
    RefreshTokenServiceGrpc.RefreshTokenServiceBlockingStub refreshTokenServiceBlockingStub;

    @GrpcClient("grpc-user-service")
    VerificationCredentialServiceGrpc.VerificationCredentialServiceBlockingStub verificationCredentialServiceBlockingStub;

    public Optional<GetUserByEmailResponse> getUserByEmail(GetUserByEmailRequest request) {
//        Set<String> requestedFields = new HashSet<>();
//        requestedFields.add(UserDto_.USER_ID);
//        requestedFields.add(UserDto_.NAME);
//        requestedFields.add(UserDto_.AVATAR);
//        requestedFields.add(UserDto_.BIO);
//        requestedFields.add(UserDto_.EMAIL);
//        requestedFields.add(UserDto_.PASSWORD);
//        requestedFields.add(UserDto_.PROVIDER);
//        requestedFields.add(UserDto_.PROVIDER_ID);

        GrpcRequest req = packRequest(GrpcGetUserByEmailRequest.newBuilder()
                .setEmail(request.getEmail())
//                .setFieldMask(FieldMask.newBuilder()
//                        .addAllPaths(requestedFields)
//                        .build())
                .build());

        GrpcResponse response = this.userServiceBlockingStub.getUserByEmail(req);

        Optional<GrpcGetUserByEmailResponse> unpackedResultOptional = unpackedResultQuery(response, GrpcGetUserByEmailResponse.class);

        if (!unpackedResultOptional.isPresent()) {
            return Optional.empty();
        }

        GrpcGetUserByEmailResponse unpackedResult = unpackedResultOptional.get();

        GrpcUser grpcUser = unpackedResult.getUser();

        return Optional.of(GetUserByEmailResponse.builder()
                .userId(grpcUser.getUserId())
                .name(grpcUser.getName())
                .avatar(grpcUser.getAvatar())
                .bio(grpcUser.getBio())
                .email(grpcUser.getEmail())
                .password(grpcUser.getPassword())
                .provider(AuthProvider.forNumber(grpcUser.getProvider().getNumber()))
                .providerId(grpcUser.getProviderId())
                .build());
    }

    public Optional<GetUserByUserIdResponse> getUserByUserId(GetUserByUserIdRequest request) {
//        Set<String> requestedFields = new HashSet<>();
//        requestedFields.add(UserDto_.USER_ID);
//        requestedFields.add(UserDto_.NAME);
//        requestedFields.add(UserDto_.AVATAR);
//        requestedFields.add(UserDto_.BIO);
//        requestedFields.add(UserDto_.EMAIL);
//        requestedFields.add(UserDto_.PASSWORD);
//        requestedFields.add(UserDto_.PROVIDER);
//        requestedFields.add(UserDto_.PROVIDER_ID);

        GrpcRequest req = packRequest(GrpcGetUserByUserIdRequest.newBuilder()
                .setUserId(request.getUserId())
//                .setFieldMask(FieldMask.newBuilder()
//                        .addAllPaths(requestedFields)
//                        .build())
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
                .provider(AuthProvider.forNumber(grpcUser.getProvider().getNumber()))
                .providerId(grpcUser.getProviderId())
                .build());
    }

    public CreateUserResponse createUser(CreateUserRequest request) {
        GrpcRequest req = packRequest(GrpcCreateUserRequest.newBuilder()
                .setName(request.getName())
                .setAvatar(request.getAvatar())
                .setEmail(request.getEmail())
                .setPassword(request.getPassword())
                .setProvider(GrpcAuthProvider.forNumber(request.getProvider().getNumber()))
                .setProviderId(request.getProviderId())
                .build());

        GrpcResponse response = this.userServiceBlockingStub.createUser(req);

        GrpcCreateUserResponse unpackedResult = unpackedResultCommand(response, GrpcCreateUserResponse.class);

        return CreateUserResponse.builder()
                .userId(unpackedResult.getUserId())
                .build();
    }

    public UpdateUserByUserIdResponse updateUserByUserIdRequest(UpdateUserByUserIdRequest request) {
//        Set<String> requestedFields = new HashSet<>();
//        requestedFields.add(UserDto_.NAME);
//        requestedFields.add(UserDto_.AVATAR);

        GrpcRequest req = packRequest(GrpcUpdateUserByUserIdRequest.newBuilder()
                .setUserId(request.getUserId())
                .setName(request.getName())
                .setAvatar(request.getAvatar())
                .setBio(request.getBio())
//                .setUpdate(GrpcUserUpdateOperation.newBuilder()
//                        .setName(request.getName())
//                        .setAvatar(request.getAvatar())
//                        .build())
//                .setUpdateMask(FieldMask.newBuilder()
//                        .addAllPaths(requestedFields)
//                        .build())
                .build());

        GrpcResponse response = this.userServiceBlockingStub.updateUserByUserId(req);

        GrpcUpdateUserByUserIdResponse unpackedResult = unpackedResultCommand(response, GrpcUpdateUserByUserIdResponse.class);

        return UpdateUserByUserIdResponse.builder()
                .userId(unpackedResult.getUserId())
                .build();
    }

    public ExistsUserByEmailResponse existsUserByEmail(ExistsUserByEmailRequest request) {
        GrpcRequest req = packRequest(GrpcExistsUserByEmailRequest.newBuilder()
                .setEmail(request.getEmail())
                .build());

        GrpcResponse response = this.userServiceBlockingStub.existsUserByEmail(req);

        Optional<GrpcExistsUserByEmailResponse> unpackedResultOptional = unpackedResultQuery(response, GrpcExistsUserByEmailResponse.class);

        ExistsUserByEmailResponse existsUserByEmailResponse = new ExistsUserByEmailResponse();

        if (!unpackedResultOptional.isPresent()) {
            existsUserByEmailResponse.setExists(false);
            return existsUserByEmailResponse;
        }

        GrpcExistsUserByEmailResponse unpackedResult = unpackedResultOptional.get();

        existsUserByEmailResponse.setExists(unpackedResult.getExists());

        return existsUserByEmailResponse;
    }

    public CreateRefreshTokenResponse createRefreshToken(CreateRefreshTokenRequest request) {
        GrpcRequest req = packRequest(GrpcCreateRefreshTokenRequest.newBuilder()
                .setRefreshToken(request.getRefreshToken())
                .setExpiresAt(request.getExpiresAt().toString())
                .setUserId(request.getUserId())
                .build());

        GrpcResponse response = this.refreshTokenServiceBlockingStub.createRefreshToken(req);

        GrpcCreateRefreshTokenResponse unpackedResult = unpackedResultCommand(response, GrpcCreateRefreshTokenResponse.class);

        return CreateRefreshTokenResponse.builder()
                .refreshTokenId(unpackedResult.getRefreshTokenId())
                .build();
    }

    public DeleteRefreshTokenByRefreshTokenResponse deleteRefreshTokenByRefreshToken(DeleteRefreshTokenByRefreshTokenRequest request) {
        GrpcRequest req = packRequest(GrpcDeleteRefreshTokenByRefreshTokenRequest.newBuilder()
                .setRefreshToken(request.getRefreshToken())
                .build());

        GrpcResponse response = this.refreshTokenServiceBlockingStub.deleteRefreshTokenByRefreshToken(req);

        GrpcDeleteRefreshTokenByRefreshTokenResponse unpackedResult = unpackedResultCommand(response, GrpcDeleteRefreshTokenByRefreshTokenResponse.class);

        return DeleteRefreshTokenByRefreshTokenResponse.builder()
                .deleted(unpackedResult.getDeleted())
                .build();
    }

    public Optional<GetRefreshTokenByRefreshTokenResponse> getRefreshTokenByRefreshToken(GetRefreshTokenByRefreshTokenRequest request) {
        GrpcRequest req = packRequest(GrpcGetRefreshTokenByRefreshTokenRequest.newBuilder()
                .setRefreshToken(request.getRefreshToken())
                .build());

        GrpcResponse response = this.refreshTokenServiceBlockingStub.getRefreshTokenByRefreshToken(req);

        Optional<GrpcGetRefreshTokenByRefreshTokenResponse> unpackedResultOptional = unpackedResultQuery(response, GrpcGetRefreshTokenByRefreshTokenResponse.class);

        if (!unpackedResultOptional.isPresent()) {
            return Optional.empty();
        }

        GrpcGetRefreshTokenByRefreshTokenResponse unpackedResult = unpackedResultOptional.get();

        GrpcRefreshToken grpcRefreshToken = unpackedResult.getRefreshToken();

        return Optional.of(GetRefreshTokenByRefreshTokenResponse.builder()
                .refreshTokenId(grpcRefreshToken.getRefreshTokenId())
                .refreshToken(grpcRefreshToken.getRefreshToken())
                .expiresAt(Date.from(Instant.parse(grpcRefreshToken.getExpiresAt())))
                .status(RefreshTokenStatus.forNumber(grpcRefreshToken.getStatus().getNumber()))
                .userId(grpcRefreshToken.getUserId())
                .build());
    }

    public DeleteAllRefreshTokenByUserIdResponse deleteAllRefreshTokenByUserId(DeleteAllRefreshTokenByUserIdRequest request) {
        GrpcRequest req = packRequest(GrpcDeleteAllRefreshTokenByUserIdRequest.newBuilder()
                .setUserId(request.getUserId())
                .build());

        GrpcResponse response = this.refreshTokenServiceBlockingStub.deleteAllRefreshTokenByUserId(req);

        GrpcDeleteAllRefreshTokenByUserIdResponse unpackedResult = unpackedResultCommand(response, GrpcDeleteAllRefreshTokenByUserIdResponse.class);

        return DeleteAllRefreshTokenByUserIdResponse.builder()
                .deleted(unpackedResult.getDeleted())
                .build();
    }

    public UpdateRefreshTokenByRefreshTokenIdResponse updateRefreshTokenByRefreshTokenId(UpdateRefreshTokenByRefreshTokenIdRequest request) {
        GrpcRequest req = packRequest(GrpcUpdateRefreshTokenByRefreshTokenIdRequest.newBuilder()
                .setRefreshTokenId(request.getRefreshTokenId())
                .setStatus(GrpcRefreshTokenStatus.forNumber(request.getStatus().getNumber()))
                .build());

        GrpcResponse response = this.refreshTokenServiceBlockingStub.updateRefreshTokenByRefreshTokenId(req);

        GrpcUpdateRefreshTokenByRefreshTokenIdResponse unpackedResult = unpackedResultCommand(response, GrpcUpdateRefreshTokenByRefreshTokenIdResponse.class);

        return UpdateRefreshTokenByRefreshTokenIdResponse.builder()
                .refreshTokenId(unpackedResult.getRefreshTokenId())
                .build();
    }

    public VerificationCredentialByVerificationTokenResponse verificationCredentialByVerificationToken(VerificationCredentialByVerificationTokenRequest request) {
        GrpcRequest req = packRequest(GrpcVerificationCredentialByVerificationTokenRequest.newBuilder()
                .setVerificationToken(request.getVerificationToken())
                .build());

        GrpcResponse response = this.verificationCredentialServiceBlockingStub.verificationCredentialByVerificationToken(req);

        GrpcVerificationCredentialByVerificationTokenResponse unpackedResult = unpackedResultCommand(response, GrpcVerificationCredentialByVerificationTokenResponse.class);

        return VerificationCredentialByVerificationTokenResponse.builder()
                .verified(unpackedResult.getVerified())
                .build();
    }

    public VerificationCredentialByVerificationOtpResponse verificationCredentialByVerificationOtp(VerificationCredentialByVerificationOtpRequest request) {
        GrpcRequest req = packRequest(GrpcVerificationCredentialByVerificationOtpRequest.newBuilder()
                .setVerificationOtp(request.getVerificationOtp())
                .build());

        GrpcResponse response = this.verificationCredentialServiceBlockingStub.verificationCredentialByVerificationOtp(req);

        GrpcVerificationCredentialByVerificationOtpResponse unpackedResult = unpackedResultCommand(response, GrpcVerificationCredentialByVerificationOtpResponse.class);

        return VerificationCredentialByVerificationOtpResponse.builder()
                .verified(unpackedResult.getVerified())
                .build();
    }

    public ReissueVerificationCredentialByUserIdResponse reissueVerificationCredentialByUserId(ReissueVerificationCredentialByUserIdRequest request) {
        GrpcRequest req = packRequest(GrpcReissueVerificationCredentialByUserIdRequest.newBuilder()
                .setUserId(request.getUserId())
                .build());

        GrpcResponse response = this.verificationCredentialServiceBlockingStub.reissueVerificationCredentialByUserId(req);

        GrpcReissueVerificationCredentialByUserIdResponse unpackedResult = unpackedResultCommand(response, GrpcReissueVerificationCredentialByUserIdResponse.class);

        return ReissueVerificationCredentialByUserIdResponse.builder()
                .reissue(unpackedResult.getReissue())
                .build();
    }

    public ChangeUserPasswordByUserIdResponse changeUserPasswordByUserId(ChangeUserPasswordByUserIdRequest request) {
        GrpcRequest req = packRequest(GrpcChangeUserPasswordByUserIdRequest.newBuilder()
                .setUserId(request.getUserId())
                .setOldPassword(request.getOldPassword())
                .setNewPassword(request.getNewPassword())
                .build());

        GrpcResponse response = this.userServiceBlockingStub.changeUserPasswordByUserId(req);

        GrpcChangeUserPasswordByUserIdResponse unpackedResult = unpackedResultCommand(response, GrpcChangeUserPasswordByUserIdResponse.class);

        return ChangeUserPasswordByUserIdResponse.builder()
                .userId(unpackedResult.getUserId())
                .build();
    }
}
