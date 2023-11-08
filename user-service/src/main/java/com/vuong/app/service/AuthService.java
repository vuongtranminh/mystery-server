package com.vuong.app.service;

import com.vuong.app.config.MysteryJdbc;
import com.vuong.app.doman.AuthProvider;
import com.vuong.app.doman.VerificationCredential;
import com.vuong.app.event.CreateUserEvent;
import com.vuong.app.kafka.KafKaProducerService;
import com.vuong.app.redis.repository.UserRepository;
import com.vuong.app.v1.GrpcErrorCode;
import com.vuong.app.v1.GrpcErrorResponse;
import com.vuong.app.v1.auth.*;
import io.grpc.Metadata;
import io.grpc.Status;
import io.grpc.protobuf.ProtoUtils;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@GrpcService
@Slf4j
@RequiredArgsConstructor
public class AuthService extends AuthServiceGrpc.AuthServiceImplBase {

    private final MysteryJdbc mysteryJdbc;
    private final VerificationCredentialService verificationCredentialService;
    private final UserRepository userRepository;
    private final KafKaProducerService producer;

    @Override
    public void createUserSocial(GrpcCreateUserSocialRequest request, StreamObserver<GrpcCreateUserSocialResponse> responseObserver) {

        AuthProvider authProvider = AuthProvider.forNumber(request.getProvider().getNumber());

        String insertUserQuery = "insert into tbl_user(id, name, avt_url, email, verified, provider, provider_id, created_at, updated_at) " +
                "values (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        Connection con = null;
        PreparedStatement pst1 = null;
        PreparedStatement pst2 = null;

        try {
            con = mysteryJdbc.getConnection();

            String userId = UUID.randomUUID().toString();
            String createdAt = Instant.now().toString();
            String updatedAt = createdAt;

            pst1 = con.prepareStatement(insertUserQuery);
            pst1.setString(1, userId);
            pst1.setString(2, request.getName());
            pst1.setString(3, request.getAvtUrl());
            pst1.setString(4, request.getEmail());
            pst1.setBoolean(5, true);
            pst1.setInt(6, authProvider.getNumber());
            pst1.setString(7, request.getProviderId());
            pst1.setString(8, createdAt);
            pst1.setString(9, updatedAt);

            int result1 = pst1.executeUpdate();

//            mysteryJdbc.doCommit();

            this.userRepository.saveUser(GrpcUserPrincipal.newBuilder()
                    .setUserId(userId)
                    .setEmail(request.getEmail())
                    .build());

            GrpcCreateUserSocialResponse response = GrpcCreateUserSocialResponse.newBuilder().setUserId(userId).build();

            this.producer.sendMessage("add", CreateUserEvent.builder()
                    .userId(userId)
                    .name(request.getName())
                    .avtUrl(request.getAvtUrl())
                    .build());

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (SQLException ex) {
            log.error(ex.getMessage());
            mysteryJdbc.doRollback();
        } finally {
            mysteryJdbc.closePreparedStatement(pst1, pst2);
        }
    }

    @Override
    public void createUserLocal(GrpcCreateUserLocalRequest request, StreamObserver<GrpcCreateUserLocalResponse> responseObserver) {
        String insertUserQuery = "insert into tbl_user(id, name, email, password, verified, provider, created_at, updated_at) " +
                "values (?, ?, ?, ?, ?, ?, ?, ?)";

        Connection con = null;
        PreparedStatement pst1 = null;
        PreparedStatement pst2 = null;

        try {
            con = mysteryJdbc.getConnection();

            String userId = UUID.randomUUID().toString();
            String createdAt = Instant.now().toString();
            String updatedAt = createdAt;

            pst1 = con.prepareStatement(insertUserQuery);
            pst1.setString(1, userId);
            pst1.setString(2, request.getName());
            pst1.setString(3, request.getEmail());
            pst1.setString(4, request.getPassword());
            pst1.setBoolean(5, false);
            pst1.setInt(6, AuthProvider.local.getNumber());
            pst1.setString(7, createdAt);
            pst1.setString(8, updatedAt);

            mysteryJdbc.setAutoCommit(false);
            int result1 = pst1.executeUpdate();

            Instant now = Instant.now();
            String insertVerificationCredentialQuery = "insert into tbl_verification_credential(id, verification_token, verification_otp, expire_date) " +
                    "values (?, ?, ?, ?)";

            String verificationCredentialId = userId;
            String verificationToken = this.verificationCredentialService.generateVerificationToken();
            String verificationOtp = this.verificationCredentialService.generateVerificationOtp();
            String expireDate = now.plus(1, ChronoUnit.DAYS).toString();

            pst2 = con.prepareStatement(insertVerificationCredentialQuery);
            pst2.setString(1, verificationCredentialId);
            pst2.setString(2, verificationToken);
            pst2.setString(3, verificationOtp);
            pst2.setString(4, expireDate);

            int result2 = pst2.executeUpdate();

            VerificationCredential verificationCredential = VerificationCredential.builder()
                    .verificationCredentialId(verificationCredentialId)
                    .verificationToken(verificationToken)
                    .verificationOtp(verificationOtp)
                    .expireDate(expireDate)
                    .build();

            mysteryJdbc.doCommit();

            this.verificationCredentialService.sendMailVerify(request.getEmail(), verificationCredential);

            GrpcCreateUserLocalResponse response = GrpcCreateUserLocalResponse.newBuilder()
                    .setUserId(userId)
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (SQLException ex) {
            log.error(ex.getMessage());
            mysteryJdbc.doRollback();
        } finally {
            mysteryJdbc.closePreparedStatement(pst1, pst2);
        }
    }

    @Override
    public void getUserPrincipalByUserId(GrpcGetUserPrincipalByUserIdRequest request, StreamObserver<GrpcGetUserPrincipalByUserIdResponse> responseObserver) {
        String userQuery = "select " +
                "tbl_user.id, tbl_user.email, tbl_user.password " +
                "from tbl_user " +
                "where tbl_user.id = ?";

        Connection con = null;
        PreparedStatement pst = null;
        ResultSet rs = null;

        try {
            con = mysteryJdbc.getConnection();

            pst = con.prepareStatement(userQuery);
            pst.setString(1, request.getUserId());

            rs = pst.executeQuery();

            GrpcUserPrincipal.Builder builder = GrpcUserPrincipal.newBuilder();
            boolean hasResult = false;

            while (rs.next()) {
                hasResult = true;
                builder.setUserId(rs.getString(1));
                builder.setEmail(rs.getString(2));

                if (rs.getString(3) != null) {
                    builder.setPassword(rs.getString(3));
                }
            }

            if (!hasResult) {
                Metadata metadata = new Metadata();
                Metadata.Key<GrpcErrorResponse> responseKey = ProtoUtils.keyForProto(GrpcErrorResponse.getDefaultInstance());
                GrpcErrorCode errorCode = GrpcErrorCode.ERROR_CODE_NOT_FOUND;
                GrpcErrorResponse errorResponse = GrpcErrorResponse.newBuilder()
                        .setErrorCode(errorCode)
                        .setMessage("not found with userId")
                        .build();
                // pass the error object via metadata
                metadata.put(responseKey, errorResponse);
                responseObserver.onError(Status.NOT_FOUND.asRuntimeException(metadata));
                return;
            }

            GrpcUserPrincipal userPrincipal = builder.build();

            this.userRepository.saveUser(userPrincipal);

            GrpcGetUserPrincipalByUserIdResponse  response = GrpcGetUserPrincipalByUserIdResponse.newBuilder()
                    .setResult(userPrincipal)
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (SQLException ex) {
            log.error(ex.getMessage());
//            mysteryJdbc.doRollback();
        } finally {
            mysteryJdbc.closeResultSet(rs);
            mysteryJdbc.closePreparedStatement(pst);
        }
    }

    @Override
    public void getUserPrincipalByEmail(GrpcGetUserPrincipalByEmailRequest request, StreamObserver<GrpcGetUserPrincipalByEmailResponse> responseObserver) {
        String userQuery = "select " +
                "tbl_user.id, tbl_user.email, tbl_user.password " +
                "from tbl_user " +
                "where tbl_user.email = ?";

        Connection con = null;
        PreparedStatement pst = null;
        ResultSet rs = null;

        try {
            con = mysteryJdbc.getConnection();

            pst = con.prepareStatement(userQuery);
            pst.setString(1, request.getEmail());

            rs = pst.executeQuery();

            if (!rs.next()) {
                Metadata metadata = new Metadata();
                Metadata.Key<GrpcErrorResponse> responseKey = ProtoUtils.keyForProto(GrpcErrorResponse.getDefaultInstance());
                GrpcErrorCode errorCode = GrpcErrorCode.ERROR_CODE_NOT_FOUND;
                GrpcErrorResponse errorResponse = GrpcErrorResponse.newBuilder()
                        .setErrorCode(errorCode)
                        .setMessage("not found with email")
                        .build();
                // pass the error object via metadata
                metadata.put(responseKey, errorResponse);
                responseObserver.onError(Status.NOT_FOUND.asRuntimeException(metadata));
                return;
            }

            GrpcUserPrincipal.Builder builder = GrpcUserPrincipal.newBuilder();

            while (rs.next()) {
                builder.setUserId(rs.getString(1));
                builder.setEmail(rs.getString(2));

                if (rs.getString(3) != null) {
                    builder.setPassword(rs.getString(3));
                }
            }

            GrpcUserPrincipal userPrincipal = builder.build();

            this.userRepository.saveUser(userPrincipal);

            GrpcGetUserPrincipalByEmailResponse  response = GrpcGetUserPrincipalByEmailResponse.newBuilder()
                    .setResult(userPrincipal)
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (SQLException ex) {
            log.error(ex.getMessage());
//            mysteryJdbc.doRollback();
        } finally {
            mysteryJdbc.closeResultSet(rs);
            mysteryJdbc.closePreparedStatement(pst);
        }
    }

    @Override
    public void changeUserPasswordByUserId(GrpcChangeUserPasswordByUserIdRequest request, StreamObserver<GrpcChangeUserPasswordByUserIdResponse> responseObserver) {
        String changeUserPasswordQuery = "update tbl_user set password = ? where tbl_user.id = ? and tbl_user.password = ?";

        Connection con = null;
        PreparedStatement pst = null;

        try {
            con = mysteryJdbc.getConnection();

            pst = con.prepareStatement(changeUserPasswordQuery);
            pst.setString(1, request.getNewPassword());
            pst.setString(2, request.getUserId());
            pst.setString(3, request.getOldPassword());

            int result = pst.executeUpdate();

            if (result == 0) {
                Metadata metadata = new Metadata();
                Metadata.Key<GrpcErrorResponse> responseKey = ProtoUtils.keyForProto(GrpcErrorResponse.getDefaultInstance());
                GrpcErrorCode errorCode = GrpcErrorCode.ERROR_CODE_NOT_FOUND;
                GrpcErrorResponse errorResponse = GrpcErrorResponse.newBuilder()
                        .setErrorCode(errorCode)
                        .setMessage("not found with email")
                        .build();
                // pass the error object via metadata
                metadata.put(responseKey, errorResponse);
                responseObserver.onError(Status.NOT_FOUND.asRuntimeException(metadata));
                return;
            }

            GrpcChangeUserPasswordByUserIdResponse response = GrpcChangeUserPasswordByUserIdResponse.newBuilder()
                    .setUserId(request.getUserId())
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (SQLException ex) {
            log.error(ex.getMessage());
            mysteryJdbc.doRollback();
        } finally {
            mysteryJdbc.closePreparedStatement(pst);
        }
    }
}
