package com.vuong.app.service;

import com.vuong.app.common.ServiceHelper;
import com.vuong.app.config.MysteryJdbc;
import com.vuong.app.doman.AuthProvider;
import com.vuong.app.doman.VerificationCredential;
import com.vuong.app.redis.repository.UserRepository;
import com.vuong.app.v1.GrpcErrorCode;
import com.vuong.app.v1.GrpcRequest;
import com.vuong.app.v1.GrpcResponse;
import com.vuong.app.v1.auth.*;
import com.vuong.app.v1.user.*;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import org.apache.commons.lang3.StringUtils;

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

    @Override
    public void createUserSocial(GrpcRequest request, StreamObserver<GrpcResponse> responseObserver) {
        GrpcCreateUserSocialRequest req = ServiceHelper.unpackedRequest(request, GrpcCreateUserSocialRequest.class);

        AuthProvider authProvider = AuthProvider.forNumber(req.getProvider().getNumber());

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
            pst1.setString(2, req.getName());
            pst1.setString(3, req.getAvtUrl());
            pst1.setString(4, req.getEmail());
            pst1.setBoolean(5, true);
            pst1.setInt(6, authProvider.getNumber());
            pst1.setString(7, req.getProviderId());
            pst1.setString(8, createdAt);
            pst1.setString(9, updatedAt);

            int result1 = pst1.executeUpdate();

            this.userRepository.saveUser(GrpcUserPrincipal.newBuilder()
                    .setUserId(userId)
                    .setEmail(req.getEmail())
                    .build());

            GrpcCreateUserSocialResponse response = GrpcCreateUserSocialResponse.newBuilder().setUserId(userId).build();
            ServiceHelper.next(responseObserver, ServiceHelper.packedSuccessResponse(response));

        } catch (SQLException ex) {
            log.error(ex.getMessage());
            mysteryJdbc.doRollback();
        } finally {
            mysteryJdbc.closePreparedStatement(pst1, pst2);
        }
    }

    @Override
    public void createUserLocal(GrpcRequest request, StreamObserver<GrpcResponse> responseObserver) {
        GrpcCreateUserLocalRequest req = ServiceHelper.unpackedRequest(request, GrpcCreateUserLocalRequest.class);

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
            pst1.setString(2, req.getName());
            pst1.setString(3, req.getEmail());
            pst1.setString(4, req.getPassword());
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

            this.verificationCredentialService.sendMailVerify(req.getEmail(), verificationCredential);

        } catch (SQLException ex) {
            log.error(ex.getMessage());
            mysteryJdbc.doRollback();
        } finally {
            mysteryJdbc.closePreparedStatement(pst1, pst2);
        }
    }

    @Override
    public void getUserPrincipalByUserId(GrpcRequest request, StreamObserver<GrpcResponse> responseObserver) {
        GrpcGetUserPrincipalByUserIdRequest req = ServiceHelper.unpackedRequest(request, GrpcGetUserPrincipalByUserIdRequest.class);

        String userQuery = "select " +
                "tbl_user.id, tbl_user.email, tbl_user.password" +
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

            if (!rs.next()) {
                ServiceHelper.next(responseObserver, ServiceHelper.packedErrorResponse(GrpcErrorCode.ERROR_CODE_NOT_FOUND, "user not found with id"));
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

            GrpcGetUserPrincipalByUserIdResponse  response = GrpcGetUserPrincipalByUserIdResponse.newBuilder()
                    .setResult(userPrincipal)
                    .build();

            ServiceHelper.next(responseObserver, ServiceHelper.packedSuccessResponse(response));
        } catch (SQLException ex) {
            log.error(ex.getMessage());
//            mysteryJdbc.doRollback();
        } finally {
            mysteryJdbc.closeResultSet(rs);
            mysteryJdbc.closePreparedStatement(pst);
        }
    }

    @Override
    public void getUserPrincipalByEmail(GrpcRequest request, StreamObserver<GrpcResponse> responseObserver) {
        GrpcGetUserPrincipalByEmailRequest req = ServiceHelper.unpackedRequest(request, GrpcGetUserPrincipalByEmailRequest.class);

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
            pst.setString(1, req.getEmail());

            rs = pst.executeQuery();

            if (!rs.next()) {
                ServiceHelper.next(responseObserver, ServiceHelper.packedErrorResponse(GrpcErrorCode.ERROR_CODE_NOT_FOUND, "user not found with id"));
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

            ServiceHelper.next(responseObserver, ServiceHelper.packedSuccessResponse(response));
        } catch (SQLException ex) {
            log.error(ex.getMessage());
//            mysteryJdbc.doRollback();
        } finally {
            mysteryJdbc.closeResultSet(rs);
            mysteryJdbc.closePreparedStatement(pst);
        }
    }
}
