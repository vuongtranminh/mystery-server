package com.vuong.app.service;

import com.vuong.app.common.ServiceHelper;
import com.vuong.app.config.MysteryJdbc;
import com.vuong.app.v1.GrpcErrorCode;
import com.vuong.app.v1.GrpcRequest;
import com.vuong.app.v1.GrpcResponse;
import com.vuong.app.v1.user.*;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

@GrpcService
@Slf4j
@RequiredArgsConstructor
public class RefreshTokenService extends RefreshTokenServiceGrpc.RefreshTokenServiceImplBase {

    private final MysteryJdbc mysteryJdbc;

    @Override
    public void createRefreshToken(GrpcRequest request, StreamObserver<GrpcResponse> responseObserver) {
        GrpcCreateRefreshTokenRequest req = ServiceHelper.unpackedRequest(request, GrpcCreateRefreshTokenRequest.class);

        String existsUserQuery = "select 1 from tbl_user where tbl_user.id = ?";

        String insertRefreshTokenQuery = "insert into tbl_refresh_token(id, refresh_token, expires_at, user_id, status) values (?, ?, ?, ?, ?)";

        Connection con = null;
        PreparedStatement pst1 = null;
        PreparedStatement pst2 = null;
        ResultSet rs1 = null;
        ResultSet rs2 = null;

        try {
            con = mysteryJdbc.getConnection();
            pst1 = con.prepareStatement(existsUserQuery);
            pst1.setString(1, req.getUserId());

            rs1 = pst1.executeQuery();

            boolean existsUser = rs1.next();

            if (!existsUser) {
                ServiceHelper.next(responseObserver, ServiceHelper.packedErrorResponse(GrpcErrorCode.ERROR_CODE_NOT_FOUND, "user not found with id"));
                return;
            }

            String refreshTokenId = UUID.randomUUID().toString();

            pst2 = con.prepareStatement(insertRefreshTokenQuery);
            pst2.setString(1, refreshTokenId);
            pst2.setString(2, req.getRefreshToken());
            pst2.setString(3, req.getExpiresAt());
            pst2.setString(4, req.getUserId());
            pst2.setInt(5, GrpcRefreshTokenStatus.REFRESH_TOKEN_READY.getNumber());

            pst2.executeUpdate();

            GrpcCreateRefreshTokenResponse response = GrpcCreateRefreshTokenResponse.newBuilder().setRefreshTokenId(refreshTokenId).build();

            ServiceHelper.next(responseObserver, ServiceHelper.packedSuccessResponse(response));
        } catch (SQLException ex) {
            mysteryJdbc.doRollback();
        } finally {
            mysteryJdbc.closeResultSet(rs1, rs2);
            mysteryJdbc.closePreparedStatement(pst1, pst2);
        }
    }

    @Override
    public void getRefreshTokenByRefreshToken(GrpcRequest request, StreamObserver<GrpcResponse> responseObserver) {
        GrpcGetRefreshTokenByRefreshTokenRequest req = ServiceHelper.unpackedRequest(request, GrpcGetRefreshTokenByRefreshTokenRequest.class);

        String refreshTokenQuery = "select " +
                "tbl_refresh_token.id, tbl_refresh_token.refresh_token, tbl_refresh_token.expires_at, tbl_refresh_token.user_id, tbl_refresh_token.status " +
                "from tbl_refresh_token " +
                "where tbl_refresh_token.refresh_token = ?";

        Connection con = null;
        PreparedStatement pst = null;
        ResultSet rs = null;

        try {
            con = mysteryJdbc.getConnection();

            pst = con.prepareStatement(refreshTokenQuery);
            pst.setString(1, req.getRefreshToken());

            rs = pst.executeQuery();
            GrpcGetRefreshTokenByRefreshTokenResponse response = null;

            while (rs.next()) {
                response = GrpcGetRefreshTokenByRefreshTokenResponse.newBuilder()
                        .setResult(GrpcRefreshToken.newBuilder()
                                .setRefreshTokenId(rs.getString(1))
                                .setRefreshToken(rs.getString(2))
                                .setExpiresAt(rs.getString(3))
                                .setUserId(rs.getString(4))
                                .setStatus(GrpcRefreshTokenStatus.forNumber(rs.getInt(5)))
                                .build())
                        .build();
            }

            if (response == null) {
                ServiceHelper.next(responseObserver, ServiceHelper.packedErrorResponse(GrpcErrorCode.ERROR_CODE_NOT_FOUND, "refreshToken not found with refreshToken"));
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
    public void updateRefreshTokenStatusByRefreshTokenId(GrpcRequest request, StreamObserver<GrpcResponse> responseObserver) {
        GrpcUpdateRefreshTokenStatusByRefreshTokenIdRequest req = ServiceHelper.unpackedRequest(request, GrpcUpdateRefreshTokenStatusByRefreshTokenIdRequest.class);

        String updateRefreshTokenStatusQuery = "update tbl_refresh_token set status = ? where tbl_refresh_token.id = ?";

        Connection con = null;
        PreparedStatement pst = null;

        try {
            con = mysteryJdbc.getConnection();

            pst = con.prepareStatement(updateRefreshTokenStatusQuery);
            pst.setInt(1, req.getStatus().getNumber());
            pst.setString(2, req.getRefreshTokenId());

            int result = pst.executeUpdate();

            if (result == 0) {
                ServiceHelper.next(responseObserver, ServiceHelper.packedErrorResponse(GrpcErrorCode.ERROR_CODE_NOT_FOUND, "refreshToken not found with refreshToken"));
                return;
            }

            GrpcUpdateRefreshTokenStatusByRefreshTokenIdResponse response = GrpcUpdateRefreshTokenStatusByRefreshTokenIdResponse.newBuilder()
                    .setRefreshTokenId(req.getRefreshTokenId())
                    .build();

            ServiceHelper.next(responseObserver, ServiceHelper.packedSuccessResponse(response));
        } catch (SQLException ex) {
            mysteryJdbc.doRollback();
        } finally {
            mysteryJdbc.closePreparedStatement(pst);
        }
    }

    @Override
    public void deleteRefreshTokenByRefreshToken(GrpcRequest request, StreamObserver<GrpcResponse> responseObserver) {
        GrpcDeleteRefreshTokenByRefreshTokenRequest req = ServiceHelper.unpackedRequest(request, GrpcDeleteRefreshTokenByRefreshTokenRequest.class);

        String deleteRefreshTokenQuery = "delete tbl_refresh_token where tbl_refresh_token.refresh_token = ?";

        Connection con = null;
        PreparedStatement pst = null;

        try {
            con = mysteryJdbc.getConnection();

            pst = con.prepareStatement(deleteRefreshTokenQuery);
            pst.setString(1, req.getRefreshToken());

            int result = pst.executeUpdate();

            GrpcDeleteRefreshTokenByRefreshTokenResponse response = GrpcDeleteRefreshTokenByRefreshTokenResponse.newBuilder()
                    .setDeleted(true)
                    .build();

            ServiceHelper.next(responseObserver, ServiceHelper.packedSuccessResponse(response));
        } catch (SQLException ex) {
            mysteryJdbc.doRollback();
        } finally {
            mysteryJdbc.closePreparedStatement(pst);
        }
    }

    @Override
    public void deleteAllRefreshTokenByUserId(GrpcRequest request, StreamObserver<GrpcResponse> responseObserver) {
        GrpcDeleteAllRefreshTokenByUserIdRequest req = ServiceHelper.unpackedRequest(request, GrpcDeleteAllRefreshTokenByUserIdRequest.class);

        String deleteRefreshTokensQuery = "delete tbl_refresh_token where tbl_refresh_token.user_id = ?";

        Connection con = null;
        PreparedStatement pst = null;

        try {
            con = mysteryJdbc.getConnection();

            pst = con.prepareStatement(deleteRefreshTokensQuery);
            pst.setString(1, req.getUserId());

            int result = pst.executeUpdate();

            GrpcDeleteAllRefreshTokenByUserIdResponse response = GrpcDeleteAllRefreshTokenByUserIdResponse.newBuilder()
                    .setDeleted(true)
                    .build();

            ServiceHelper.next(responseObserver, ServiceHelper.packedSuccessResponse(response));
        } catch (SQLException ex) {
            mysteryJdbc.doRollback();
        } finally {
            mysteryJdbc.closePreparedStatement(pst);
        }
    }
}
