package com.vuong.app.grpc.service;

import com.vuong.app.business.auth.model.*;
import com.vuong.app.exception.wrapper.WTuxException;
import com.vuong.app.grpc.message.auth.*;
import com.vuong.app.v1.*;
import com.vuong.app.v1.auth.*;
import com.vuong.app.v1.user.*;
import io.grpc.Metadata;
import io.grpc.Status;
import io.grpc.protobuf.ProtoUtils;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Date;
import java.util.Optional;

@Service
@Slf4j
public class AuthClientService {

    @GrpcClient("grpc-user-service")
    UserServiceGrpc.UserServiceBlockingStub userServiceBlockingStub;

    @GrpcClient("grpc-user-service")
    AuthServiceGrpc.AuthServiceBlockingStub authServiceBlockingStub;

    @GrpcClient("grpc-user-service")
    VerificationCredentialServiceGrpc.VerificationCredentialServiceBlockingStub verificationCredentialServiceBlockingStub;

    public Optional<GetUserPrincipalByEmailResponse> getUserPrincipalByEmail(GetUserPrincipalByEmailRequest request) {
        try {
            GrpcGetUserPrincipalByEmailResponse response = this.authServiceBlockingStub.getUserPrincipalByEmail(GrpcGetUserPrincipalByEmailRequest.newBuilder()
                    .setEmail(request.getEmail())
                    .build());

            GrpcUserPrincipal grpcUserPrincipal = response.getResult();

            return Optional.of(GetUserPrincipalByEmailResponse.builder()
                    .userId(grpcUserPrincipal.getUserId())
                    .email(grpcUserPrincipal.getEmail())
                    .password(grpcUserPrincipal.getPassword())
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

    public Optional<GetUserPrincipalByUserIdResponse> getUserPrincipalByUserId(GetUserPrincipalByUserIdRequest request) {
        try {
            GrpcGetUserPrincipalByUserIdResponse response = this.authServiceBlockingStub.getUserPrincipalByUserId(GrpcGetUserPrincipalByUserIdRequest.newBuilder()
                    .setUserId(request.getUserId())
                    .build());

            GrpcUserPrincipal grpcUserPrincipal = response.getResult();

            return Optional.of(GetUserPrincipalByUserIdResponse.builder()
                    .userId(grpcUserPrincipal.getUserId())
                    .email(grpcUserPrincipal.getEmail())
                    .password(grpcUserPrincipal.getPassword())
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

    public CreateUserSocialResponse createUserSocial(CreateUserSocialRequest request) {
        try {
            GrpcCreateUserSocialResponse response = this.authServiceBlockingStub.createUserSocial(GrpcCreateUserSocialRequest.newBuilder()
                    .setName(request.getName())
                    .setAvtUrl(request.getAvtUrl())
                    .setEmail(request.getEmail())
                    .setProvider(GrpcAuthProviderSocial.forNumber(request.getProvider().getNumber()))
                    .setProviderId(request.getProviderId())
                    .build());

            return CreateUserSocialResponse.builder()
                    .userId(response.getUserId())
                    .build();

        } catch (Exception ex) {
            Metadata metadata = Status.trailersFromThrowable(ex);
            GrpcErrorResponse errorResponse = metadata.get(ProtoUtils.keyForProto(GrpcErrorResponse.getDefaultInstance()));
            log.error(errorResponse.getErrorCode() + " : " + errorResponse.getMessage());

            throw new WTuxException(errorResponse);
        }
    }

    public CreateUserLocalResponse createUserLocal(CreateUserLocalRequest request) {
        try {
            GrpcCreateUserLocalResponse response = this.authServiceBlockingStub.createUserLocal(GrpcCreateUserLocalRequest.newBuilder()
                    .setName(request.getName())
                    .setEmail(request.getEmail())
                    .setPassword(request.getPassword())
                    .build());

            return CreateUserLocalResponse.builder()
                    .userId(response.getUserId())
                    .build();

        } catch (Exception ex) {
            Metadata metadata = Status.trailersFromThrowable(ex);
            GrpcErrorResponse errorResponse = metadata.get(ProtoUtils.keyForProto(GrpcErrorResponse.getDefaultInstance()));
            log.error(errorResponse.getErrorCode() + " : " + errorResponse.getMessage());

            throw new WTuxException(errorResponse);
        }
    }

    public VerificationCredentialByVerificationTokenResponse verificationCredentialByVerificationToken(VerificationCredentialByVerificationTokenRequest request) {
        try {
            GrpcVerificationCredentialByVerificationTokenResponse response = this.verificationCredentialServiceBlockingStub.verificationCredentialByVerificationToken(GrpcVerificationCredentialByVerificationTokenRequest.newBuilder()
                    .setVerificationToken(request.getVerificationToken())
                    .build());

            return VerificationCredentialByVerificationTokenResponse.builder()
                    .verified(response.getVerified())
                    .build();

        } catch (Exception ex) {
            Metadata metadata = Status.trailersFromThrowable(ex);
            GrpcErrorResponse errorResponse = metadata.get(ProtoUtils.keyForProto(GrpcErrorResponse.getDefaultInstance()));
            log.error(errorResponse.getErrorCode() + " : " + errorResponse.getMessage());

            throw new WTuxException(errorResponse);
        }
    }

    public VerificationCredentialByVerificationOtpResponse verificationCredentialByVerificationOtp(VerificationCredentialByVerificationOtpRequest request) {
        try {
            GrpcVerificationCredentialByVerificationOtpResponse response = this.verificationCredentialServiceBlockingStub.verificationCredentialByVerificationOtp(GrpcVerificationCredentialByVerificationOtpRequest.newBuilder()
                    .setVerificationOtp(request.getVerificationOtp())
                    .build());

            return VerificationCredentialByVerificationOtpResponse.builder()
                    .verified(response.getVerified())
                    .build();

        } catch (Exception ex) {
            Metadata metadata = Status.trailersFromThrowable(ex);
            GrpcErrorResponse errorResponse = metadata.get(ProtoUtils.keyForProto(GrpcErrorResponse.getDefaultInstance()));
            log.error(errorResponse.getErrorCode() + " : " + errorResponse.getMessage());

            throw new WTuxException(errorResponse);
        }
    }

    public ReissueVerificationCredentialByUserIdResponse reissueVerificationCredentialByUserId(ReissueVerificationCredentialByUserIdRequest request) {
        try {
            GrpcReissueVerificationCredentialByUserIdResponse response = this.verificationCredentialServiceBlockingStub.reissueVerificationCredentialByUserId(GrpcReissueVerificationCredentialByUserIdRequest.newBuilder()
                    .setUserId(request.getUserId())
                    .build());

            return ReissueVerificationCredentialByUserIdResponse.builder()
                    .reissue(response.getReissue())
                    .build();

        } catch (Exception ex) {
            Metadata metadata = Status.trailersFromThrowable(ex);
            GrpcErrorResponse errorResponse = metadata.get(ProtoUtils.keyForProto(GrpcErrorResponse.getDefaultInstance()));
            log.error(errorResponse.getErrorCode() + " : " + errorResponse.getMessage());

            throw new WTuxException(errorResponse);
        }
    }

    public ChangeUserPasswordByUserIdResponse changeUserPasswordByUserId(ChangeUserPasswordByUserIdRequest request) {
        try {
            GrpcChangeUserPasswordByUserIdResponse response = this.authServiceBlockingStub.changeUserPasswordByUserId(GrpcChangeUserPasswordByUserIdRequest.newBuilder()
                    .setUserId(request.getUserId())
                    .setOldPassword(request.getOldPassword())
                    .setNewPassword(request.getNewPassword())
                    .build());

            return ChangeUserPasswordByUserIdResponse.builder()
                    .userId(response.getUserId())
                    .build();

        } catch (Exception ex) {
            Metadata metadata = Status.trailersFromThrowable(ex);
            GrpcErrorResponse errorResponse = metadata.get(ProtoUtils.keyForProto(GrpcErrorResponse.getDefaultInstance()));
            log.error(errorResponse.getErrorCode() + " : " + errorResponse.getMessage());

            throw new WTuxException(errorResponse);
        }
    }
}
