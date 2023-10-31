package com.vuong.app.service;

import com.vuong.app.common.ServiceHelper;
import com.vuong.app.config.MysteryJdbc;
import com.vuong.app.doman.AuthProvider;
import com.vuong.app.doman.VerificationCredential;
import com.vuong.app.v1.*;
import com.vuong.app.v1.user.*;
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
public class UserService extends UserServiceGrpc.UserServiceImplBase {

    private final MysteryJdbc mysteryJdbc;
    private final VerificationCredentialService verificationCredentialService;

    @Override
    public void createUser(GrpcRequest request, StreamObserver<GrpcResponse> responseObserver) {
        GrpcCreateUserRequest req = ServiceHelper.unpackedRequest(request, GrpcCreateUserRequest.class);

        AuthProvider authProvider = AuthProvider.forNumber(req.getProvider().getNumber());
        boolean isLocal = authProvider.getNumber() == AuthProvider.local.getNumber();

        String insertUserQuery = "insert into tbl_user(id, name, avt_url, email, password, verified, provider, provider_id, created_at, updated_at) " +
                "values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

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
            pst1.setString(2, req.getName());
            pst1.setString(3, req.getAvtUrl());
            pst1.setString(4, req.getEmail());
            pst1.setString(5, isLocal ? req.getPassword() : null);
            pst1.setBoolean(6, isLocal ? false : true);
            pst1.setInt(7, authProvider.getNumber());
            pst1.setString(8, isLocal ? null : req.getProviderId());
            pst1.setString(9, createdAt);
            pst1.setString(10, updatedAt);

            mysteryJdbc.setAutoCommit(false);
            int result1 = pst1.executeUpdate();

            if (!isLocal) {
                GrpcCreateUserResponse response = GrpcCreateUserResponse.newBuilder().setUserId(userId).build();
                ServiceHelper.next(responseObserver, ServiceHelper.packedSuccessResponse(response));
                return;
            }

            Instant now = Instant.now();
            String insertVerificationCredentialQuery = "insert into tbl_verification_credential(id, verification_token, verification_otp, expire_date, user_id) " +
                    "values (?, ?, ?, ?, ?)";

            String verificationCredentialId = UUID.randomUUID().toString();
            String verificationToken = this.verificationCredentialService.generateVerificationToken();
            String verificationOtp = this.verificationCredentialService.generateVerificationOtp();
            String expireDate = now.plus(1, ChronoUnit.DAYS).toString();

            pst2 = con.prepareStatement(insertVerificationCredentialQuery);
            pst2.setString(1, verificationCredentialId);
            pst2.setString(2, verificationToken);
            pst2.setString(3, verificationOtp);
            pst2.setString(4, expireDate);
            pst2.setString(5, userId);

            int result2 = pst2.executeUpdate();

            VerificationCredential verificationCredential = VerificationCredential.builder()
                    .verificationCredentialId(verificationCredentialId)
                    .verificationToken(verificationToken)
                    .verificationOtp(verificationOtp)
                    .expireDate(expireDate)
                    .userId(userId)
                    .build();

            mysteryJdbc.doCommit();

            this.verificationCredentialService.sendMailVerify(req.getEmail(), verificationCredential);

        } catch (SQLException ex) {
            mysteryJdbc.doRollback();
        } finally {
            mysteryJdbc.closePreparedStatement(pst1, pst2);
        }
    }

    @Override
    public void updateUserByUserId(GrpcRequest request, StreamObserver<GrpcResponse> responseObserver) {
        GrpcUpdateUserByUserIdRequest req = ServiceHelper.unpackedRequest(request, GrpcUpdateUserByUserIdRequest.class);

        String updateUserQuery = "update tbl_user set name = ?, avt_url = ?, bio = ? where tbl_user.id = ?";

        Connection con = null;
        PreparedStatement pst = null;

        try {
            con = mysteryJdbc.getConnection();

            pst = con.prepareStatement(updateUserQuery);
            pst.setString(1, req.getName());
            pst.setString(2, req.getAvtUrl());
            pst.setString(3, req.getBio());
            pst.setString(4, req.getUserId());

            int result = pst.executeUpdate();

            if (result == 0) {
                ServiceHelper.next(responseObserver, ServiceHelper.packedErrorResponse(GrpcErrorCode.ERROR_CODE_NOT_FOUND, "user not found with id"));
                return;
            }

            GrpcUpdateUserByUserIdResponse response = GrpcUpdateUserByUserIdResponse.newBuilder().setUserId(req.getUserId()).build();

            ServiceHelper.next(responseObserver, ServiceHelper.packedSuccessResponse(response));
        } catch (SQLException ex) {
            mysteryJdbc.doRollback();
        } finally {
            mysteryJdbc.closePreparedStatement(pst);
        }
    }

    @Override
    public void changeUserPasswordByUserId(GrpcRequest request, StreamObserver<GrpcResponse> responseObserver) {
        GrpcChangeUserPasswordByUserIdRequest req = ServiceHelper.unpackedRequest(request, GrpcChangeUserPasswordByUserIdRequest.class);

        String changeUserPasswordQuery = "update tbl_user set password = ? where tbl_user.id = ? and tbl_user.password = ?";

        Connection con = null;
        PreparedStatement pst = null;

        try {
            con = mysteryJdbc.getConnection();

            pst = con.prepareStatement(changeUserPasswordQuery);
            pst.setString(1, req.getNewPassword());
            pst.setString(2, req.getUserId());
            pst.setString(3, req.getOldPassword());

            int result = pst.executeUpdate();

            if (result == 0) {
                ServiceHelper.next(responseObserver, ServiceHelper.packedErrorResponse(GrpcErrorCode.ERROR_CODE_NOT_FOUND, "Old password wrong!"));
                return;
            }

            GrpcChangeUserPasswordByUserIdResponse response = GrpcChangeUserPasswordByUserIdResponse.newBuilder().setUserId(req.getUserId()).build();

            ServiceHelper.next(responseObserver, ServiceHelper.packedSuccessResponse(response));
        } catch (SQLException ex) {
            mysteryJdbc.doRollback();
        } finally {
            mysteryJdbc.closePreparedStatement(pst);
        }
    }

    @Override
    public void existsUserByEmail(GrpcRequest request, StreamObserver<GrpcResponse> responseObserver) {
        GrpcExistsUserByEmailRequest req = ServiceHelper.unpackedRequest(request, GrpcExistsUserByEmailRequest.class);

        String existsUserQuery = "select 1 from tbl_user where tbl_user.email = ?";

        Connection con = null;
        PreparedStatement pst = null;
        ResultSet rs = null;

        try {
            con = mysteryJdbc.getConnection();
            pst = con.prepareStatement(existsUserQuery);
            pst.setString(1, req.getEmail());

            rs = pst.executeQuery();

            boolean existsUser = rs.next();

            GrpcExistsUserByEmailResponse response = GrpcExistsUserByEmailResponse.newBuilder().setExists(existsUser).build();

            ServiceHelper.next(responseObserver, ServiceHelper.packedSuccessResponse(response));
        } catch (SQLException ex) {
//            mysteryJdbc.doRollback();
        } finally {
            mysteryJdbc.closeResultSet(rs);
            mysteryJdbc.closePreparedStatement(pst);
        }
    }

    @Override
    public void checkUserVerifiedByUserId(GrpcRequest request, StreamObserver<GrpcResponse> responseObserver) {
        GrpcCheckUserVerifiedByUserIdRequest req = ServiceHelper.unpackedRequest(request, GrpcCheckUserVerifiedByUserIdRequest.class);

        String checkUserVerifiedQuery = "select tbl_user.verified from tbl_user where tbl_user.id = ?";

        Connection con = null;
        PreparedStatement pst = null;
        ResultSet rs = null;

        try {
            con = mysteryJdbc.getConnection();
            pst = con.prepareStatement(checkUserVerifiedQuery);
            pst.setString(1, req.getUserId());

            rs = pst.executeQuery();

            boolean verified = false;

            while (rs.next()) {
                verified = rs.getBoolean(1);
            }

            GrpcCheckUserVerifiedByUserIdResponse response = GrpcCheckUserVerifiedByUserIdResponse.newBuilder().setVerified(verified).build();

            ServiceHelper.next(responseObserver, ServiceHelper.packedSuccessResponse(response));
        } catch (SQLException ex) {
//            mysteryJdbc.doRollback();
        } finally {
            mysteryJdbc.closeResultSet(rs);
            mysteryJdbc.closePreparedStatement(pst);
        }
    }

    @Override
    public void getUserByUserId(GrpcRequest request, StreamObserver<GrpcResponse> responseObserver) {
        GrpcGetUserByUserIdRequest req = ServiceHelper.unpackedRequest(request, GrpcGetUserByUserIdRequest.class);

        String userQuery = "select " +
                "tbl_user.id, tbl_user.name, tbl_user.avt_url, tbl_user.email, tbl_user.password, tbl_user.verified, tbl_user.provider, tbl_user.provider_id, tbl_user.created_at, tbl_user.updated_at " +
                "from tbl_user " +
                "where tbl_user.id = ?";

        Connection con = null;
        PreparedStatement pst = null;
        ResultSet rs = null;

        try {
            con = mysteryJdbc.getConnection();

            pst = con.prepareStatement(userQuery);
            pst.setString(1, req.getUserId());

            rs = pst.executeQuery();

            GrpcGetUserByUserIdResponse response = null;

            while (rs.next()) {
                response = GrpcGetUserByUserIdResponse.newBuilder()
                        .setResult(GrpcUser.newBuilder()
                                .setUserId(rs.getString(1))
                                .setName(rs.getString(2))
                                .setAvtUrl(rs.getString(3))
                                .setBio(rs.getString(4))
                                .setEmail(rs.getString(5))
                                .setPassword(rs.getString(6))
                                .setVerified(rs.getBoolean(7))
                                .setProvider(GrpcAuthProvider.forNumber(rs.getInt(8)))
                                .setProviderId(rs.getString(9))
                                .setCreatedAt(rs.getString(10))
                                .setUpdatedAt(rs.getString(11))
                                .build())
                        .build();
            }

            if (response == null) {
                ServiceHelper.next(responseObserver, ServiceHelper.packedErrorResponse(GrpcErrorCode.ERROR_CODE_NOT_FOUND, "user not found with id"));
                return;
            }

            ServiceHelper.next(responseObserver, ServiceHelper.packedSuccessResponse(response));
        } catch (SQLException ex) {
//            mysteryJdbc.doRollback();
        } finally {
            mysteryJdbc.closeResultSet(rs);
            mysteryJdbc.closePreparedStatement(pst);
        }
    }

    @Override
    public void getUserByEmail(GrpcRequest request, StreamObserver<GrpcResponse> responseObserver) {
        GrpcGetUserByEmailRequest req = ServiceHelper.unpackedRequest(request, GrpcGetUserByEmailRequest.class);

        String userQuery = "select " +
                "tbl_user.id, tbl_user.name, tbl_user.avt_url, tbl_user.email, tbl_user.password, tbl_user.verified, tbl_user.provider, tbl_user.provider_id, tbl_user.created_at, tbl_user.updated_at " +
                "from tbl_user " +
                "where tbl_user.email = ?";

        Connection con = null;
        PreparedStatement pst = null;
        ResultSet rs = null;

        try {
            con = mysteryJdbc.getConnection();

            pst = con.prepareStatement(userQuery);
            pst.setString(1, req.getEmail());

            rs = pst.executeQuery();

            GrpcGetUserByEmailResponse response = null;

            while (rs.next()) {
                response = GrpcGetUserByEmailResponse.newBuilder()
                        .setResult(GrpcUser.newBuilder()
                                .setUserId(rs.getString(1))
                                .setName(rs.getString(2))
                                .setAvtUrl(rs.getString(3))
                                .setBio(rs.getString(4))
                                .setEmail(rs.getString(5))
                                .setPassword(rs.getString(6))
                                .setVerified(rs.getBoolean(7))
                                .setProvider(GrpcAuthProvider.forNumber(rs.getInt(8)))
                                .setProviderId(rs.getString(9))
                                .setCreatedAt(rs.getString(10))
                                .setUpdatedAt(rs.getString(11))
                                .build())
                        .build();
            }

            if (response == null) {
                ServiceHelper.next(responseObserver, ServiceHelper.packedErrorResponse(GrpcErrorCode.ERROR_CODE_NOT_FOUND, "user not found with id"));
                return;
            }

            ServiceHelper.next(responseObserver, ServiceHelper.packedSuccessResponse(response));
        } catch (SQLException ex) {
//            mysteryJdbc.doRollback();
        } finally {
            mysteryJdbc.closeResultSet(rs);
            mysteryJdbc.closePreparedStatement(pst);
        }
    }
}
