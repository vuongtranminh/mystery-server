package com.vuong.app.service;

import com.vuong.app.config.MysteryJdbc;
import com.vuong.app.redis.repository.UserRepository;
import com.vuong.app.v1.*;
import com.vuong.app.v1.auth.GrpcUserPrincipal;
import com.vuong.app.v1.user.*;
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

@GrpcService
@Slf4j
@RequiredArgsConstructor
public class UserService extends UserServiceGrpc.UserServiceImplBase {

    private final MysteryJdbc mysteryJdbc;
    private final VerificationCredentialService verificationCredentialService;
    private final UserRepository userRepository;

    @Override
    public void updateUserByUserId(GrpcUpdateUserByUserIdRequest request, StreamObserver<GrpcUpdateUserByUserIdResponse> responseObserver) {
        String updateUserQuery = "update tbl_user set name = ?, avt_url = ?, bio = ? where tbl_user.id = ?";

        Connection con = null;
        PreparedStatement pst = null;

        try {
            con = mysteryJdbc.getConnection();

            pst = con.prepareStatement(updateUserQuery);
            pst.setString(1, request.getName());
            pst.setString(2, request.getAvtUrl());
            pst.setString(3, request.getBio());
            pst.setString(4, request.getUserId());

            int result = pst.executeUpdate();

            if (result == 0) {
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

            GrpcUpdateUserByUserIdResponse response = GrpcUpdateUserByUserIdResponse.newBuilder()
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

    @Override
    public void existsUserByEmail(GrpcExistsUserByEmailRequest request, StreamObserver<GrpcExistsUserByEmailResponse> responseObserver) {
        String existsUserQuery = "select 1 from tbl_user where tbl_user.email = ?";

        Connection con = null;
        PreparedStatement pst = null;
        ResultSet rs = null;

        try {
            con = mysteryJdbc.getConnection();
            pst = con.prepareStatement(existsUserQuery);
            pst.setString(1, request.getEmail());

            rs = pst.executeQuery();

            boolean existsUser = rs.next();

            GrpcExistsUserByEmailResponse response = GrpcExistsUserByEmailResponse.newBuilder().setExists(existsUser).build();

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
    public void getUserByUserId(GrpcGetUserByUserIdRequest request, StreamObserver<GrpcGetUserByUserIdResponse> responseObserver) {
        String userQuery = "select " +
                "tbl_user.id, tbl_user.name, tbl_user.avt_url, tbl_user.bio, tbl_user.email, tbl_user.password, tbl_user.verified, tbl_user.provider, tbl_user.provider_id, tbl_user.created_at, tbl_user.updated_at " +
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

            GrpcUser.Builder builder = GrpcUser.newBuilder();

            boolean hasResult = false;

            while (rs.next()) {
                hasResult = true;
                builder.setUserId(rs.getString(1));
                builder.setName(rs.getString(2));
                if (rs.getString(3) != null) {
                    builder.setAvtUrl(rs.getString(3));
                }
                if (rs.getString(4) != null) {
                    builder.setBio(rs.getString(4));
                }
                builder.setEmail(rs.getString(5));
                if (rs.getString(6) != null) {
                    builder.setPassword(rs.getString(6));
                }
                builder.setVerified(rs.getBoolean(7));
                builder.setProvider(GrpcAuthProvider.forNumber(rs.getInt(8)));
                if (rs.getString(9) != null) {
                    builder.setProviderId(rs.getString(9));
                }
                builder.setCreatedAt(rs.getString(10));
                builder.setUpdatedAt(rs.getString(11));
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

            GrpcUser user = builder.build();

            this.userRepository.saveUser(GrpcUserPrincipal.newBuilder()
                    .setUserId(user.getUserId())
                    .setEmail(user.getEmail())
                    .build());

            GrpcGetUserByUserIdResponse response = GrpcGetUserByUserIdResponse.newBuilder()
                    .setResult(user)
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
    public void getUserByEmail(GrpcGetUserByEmailRequest request, StreamObserver<GrpcGetUserByEmailResponse> responseObserver) {
        String userQuery = "select " +
                "tbl_user.id, tbl_user.name, tbl_user.avt_url, tbl_user.bio, tbl_user.email, tbl_user.password, tbl_user.verified, tbl_user.provider, tbl_user.provider_id, tbl_user.created_at, tbl_user.updated_at " +
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

            GrpcUser.Builder builder = GrpcUser.newBuilder();

            boolean hasResult = false;

            while (rs.next()) {
                hasResult = true;
                builder.setUserId(rs.getString(1));
                builder.setName(rs.getString(2));
                if (rs.getString(3) != null) {
                    builder.setAvtUrl(rs.getString(3));
                }
                if (rs.getString(4) != null) {
                    builder.setBio(rs.getString(4));
                }
                builder.setEmail(rs.getString(5));
                if (rs.getString(6) != null) {
                    builder.setPassword(rs.getString(6));
                }
                builder.setVerified(rs.getBoolean(7));
                builder.setProvider(GrpcAuthProvider.forNumber(rs.getInt(8)));
                if (rs.getString(9) != null) {
                    builder.setProviderId(rs.getString(9));
                }
                builder.setCreatedAt(rs.getString(10));
                builder.setUpdatedAt(rs.getString(11));
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

            GrpcUser user = builder.build();

            this.userRepository.saveUser(GrpcUserPrincipal.newBuilder()
                    .setUserId(user.getUserId())
                    .setEmail(user.getEmail())
                    .build());

            GrpcGetUserByEmailResponse response = GrpcGetUserByEmailResponse.newBuilder()
                    .setResult(user)
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
}
