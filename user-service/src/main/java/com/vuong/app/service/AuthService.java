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
    private final KafKaProducerService kafKaProducerService;

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

//            this.kafKaProducerService.sendMessage("create-user-key", CreateUserEvent.builder()
//                    .userId(userId)
//                    .name(request.getName())
//                    .avtUrl(request.getAvtUrl())
//                    .build());

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

//            this.kafKaProducerService.sendMessage("create-user-key", CreateUserEvent.builder()
//                    .userId(userId)
//                    .name(request.getName())
//                    .build());

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

            if (!mysteryJdbc.hasResult(rs)) {
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

            if (!mysteryJdbc.hasResult(rs)) {
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

    // auth_info (id, token_family, refresh_token, refresh_token_expire, user_id, last_logged_in, remote_addr, user_agent, status, access_token)
    // id: uuid, token_family: uuid
    // 1 user tối đa 2 token_family
    // create new add access_token to redis
    // status: đã dùng, không dùng => nếu đã dùng => xoá auth_info, xoá access_token

    /**
     * (Title) Refresh Token Automatic Reuse Detection and Revoke Refresh Tokens
     *
     * How could we handle a situation where there is a race condition between a legitimate user and a malicious one? For example:
     *
     * 🐱 Legitimate User has 🔄 Refresh Token 1 and 🔑 Access Token 1.
     *
     * 😈 Malicious User manages to steal 🔄 Refresh Token 1 from 🐱 Legitimate User.
     *
     * 🐱 Legitimate User uses 🔄 Refresh Token 1 to get a new refresh-access token pair.
     *
     * The 🚓 Auth0 Authorization Server returns 🔄 Refresh Token 2 and 🔑 Access Token 2 to 🐱 Legitimate User.
     *
     * 😈 Malicious User then attempts to use 🔄 Refresh Token 1 to get a new access token. Pure evil!
     *
     * What do you think should happen next? Would 😈 Malicious User manage to get a new access token?
     *
     * This is what happens when your identity platform has 🤖 Automatic Reuse Detection:
     *
     * The 🚓 Auth0 Authorization Server has been keeping track of all the refresh tokens descending from the original refresh token. That is, it has created a "token family".
     *
     * The 🚓 Auth0 Authorization Server recognizes that someone is reusing 🔄 Refresh Token 1 and immediately invalidates the refresh token family, including 🔄 Refresh Token 2.
     *
     * The 🚓 Auth0 Authorization Server returns an Access Denied response to 😈 Malicious User.
     *
     * 🔑 Access Token 2 expires, and 🐱 Legitimate User attempts to use 🔄 Refresh Token 2 to request a new refresh-access token pair.
     *
     * The 🚓 Auth0 Authorization Server returns an Access Denied response to 🐱 Legitimate User.
     *
     * The 🚓 Auth0 Authorization Server requires re-authentication to get new access and refresh tokens.
     *
     * It's critical for the most recently-issued refresh token to get immediately invalidated when a previously-used refresh token is sent to the authorization server. This prevents any refresh tokens in the same token family from being used to get new access tokens.
     *
     * This protection mechanism works regardless of whether the legitimate or malicious user is able to exchange 🔄 Refresh Token 1 for a new refresh-access token pair before the other. Without enforcing sender-constraint, the authorization server can't know which actor is legitimate or malicious in the event of a replay attack.
     *
     * Automatic reuse detection is a key component of a refresh token rotation strategy. The server has already invalidated the refresh token that has already been used. However, since the authorization server has no way of knowing if the legitimate user is holding the most current refresh token, it invalidates the whole token family just to be safe.
     */

    // Refresh Token Automatic Reuse Detection and Revoke Refresh Tokens

    /**
     * for new Login
     * count = select count(token_family) from tbl_auth_info where user_id = userId group by token_family
     * if (count = 2) tối đa 2 thiết bị
     *
     * id = UUID
     * token_family = UUID
     * last_logged_in = now
     * status = READY
     * insert into tbl_auth_info(id, token_family, refresh_token, refresh_token_expire, user_id, last_logged_in, remote_addr, user_agent, status, access_token) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?);
     * redis.save(accessToken)
     *
     * notify new device for user_id via email
     */

    /**
     * for logout
     * authInfo = select * from tbl_auth_info where refresh_token = refreshToken;
     * if (!authInfo) {
     *     return
     * }
     *
     * redis.delete(authInfo.accessToken)
     *
     * delete tbl_auth_info where token_family = authInfo.tokenFamily
     */

    /**
     *
     * provider accessToken by refreshToken
     * authInfo = select * from tbl_auth_info where refresh_token = refreshToken and refresh_token_expire > now;
     *
     * if (!authInfo) {
     *      return error refreshToken
     * }
     *
     * if (authInfo.status == USED) {
     *     revokeAuthInfos = select * from tbl_auth_info where token_family = authInfo.tokenFamily
     *     for (AuthInfo revokeAuthInfo : revokeAuthInfos) {
     *         redis.delete(revokeAuthInfo.accessToken)
     *     }
     *
     *     delete tbl_auth_info where token_family = authInfo.tokenFamily;
     *
     *     return lỗi -> đăng nhập lại
     * }
     *
     * update tbl_auth_info set status = USED where refresh_token = refreshToken
     * id = UUID
     * token_family = authInfo.tokenFamily
     * last_logged_in = authInfo.lastLoggedIn
     * refresh_token_expire = authInfo.refreshTokenExpire - now
     * status = READY
     * newAuthInfo = insert into tbl_auth_info(id, token_family, refresh_token, refresh_token_expire, user_id, last_logged_in, remote_addr, user_agent, status, access_token) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?);
     *
     * return newAuthInfo
     */

    /**
     * crontab 12h
     *
     * delete tbl_auth_info where refresh_token_expire < now
     */

    // insert into tbl_auth_info(id, token_family, refresh_token, refresh_token_expire, user_id, last_logged_in, remote_addr, user_agent, status, access_token) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?);

    // Reuse Detection
}
