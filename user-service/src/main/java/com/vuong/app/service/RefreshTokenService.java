package com.vuong.app.service;

import com.vuong.app.doman.AuthProvider;
import com.vuong.app.doman.RefreshToken;
import com.vuong.app.doman.RefreshTokenStatus;
import com.vuong.app.doman.User;
import com.vuong.app.jpa.query.ServiceHelper;
import com.vuong.app.repository.RefreshTokenRepository;
import com.vuong.app.repository.UserRepository;
import com.vuong.app.v1.*;
import com.vuong.app.v1.message.GrpcErrorCode;
import com.vuong.app.v1.message.GrpcRequest;
import com.vuong.app.v1.message.GrpcResponse;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;

import javax.transaction.Transactional;
import java.time.Instant;
import java.util.Optional;

@GrpcService
@Transactional
@Slf4j
@RequiredArgsConstructor
public class RefreshTokenService extends RefreshTokenServiceGrpc.RefreshTokenServiceImplBase {

    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;

    @Override
    public void createRefreshToken(GrpcRequest request, StreamObserver<GrpcResponse> responseObserver) {
        GrpcCreateRefreshTokenRequest req = ServiceHelper.unpackedRequest(request, GrpcCreateRefreshTokenRequest.class);
        Optional<User> userOptional = this.userRepository.findById(req.getUserId());

        if (!userOptional.isPresent()) {
            ServiceHelper.next(responseObserver, ServiceHelper.packedErrorResponse(GrpcErrorCode.ERROR_CODE_NOT_FOUND, "user not found with id"));
            return;
        }

        User user = userOptional.get();

        RefreshToken refreshToken = this.refreshTokenRepository.save(RefreshToken.builder()
                        .refreshToken(req.getRefreshToken())
                        .expiresAt(Instant.parse(req.getExpiresAt()))
                        .user(user)
                        .status(RefreshTokenStatus.READY)
                        .build());

        GrpcCreateRefreshTokenResponse response = GrpcCreateRefreshTokenResponse.newBuilder().setRefreshTokenId(refreshToken.getRefreshTokenId()).build();

        ServiceHelper.next(responseObserver, ServiceHelper.packedSuccessResponse(response));
    }

    @Override
    public void getRefreshTokenByRefreshToken(GrpcRequest request, StreamObserver<GrpcResponse> responseObserver) {
        GrpcGetRefreshTokenByRefreshTokenRequest req = ServiceHelper.unpackedRequest(request, GrpcGetRefreshTokenByRefreshTokenRequest.class);
        Optional<RefreshToken> refreshTokenOptional = this.refreshTokenRepository.findByRefreshToken(req.getRefreshToken());

        if (!refreshTokenOptional.isPresent()) {
            ServiceHelper.next(responseObserver, ServiceHelper.packedErrorResponse(GrpcErrorCode.ERROR_CODE_NOT_FOUND, "refreshToken not found with refreshToken"));
            return;
        }

        RefreshToken refreshToken = refreshTokenOptional.get();

        GrpcGetRefreshTokenByRefreshTokenResponse response = GrpcGetRefreshTokenByRefreshTokenResponse.newBuilder()
                .setRefreshToken(GrpcRefreshToken.newBuilder()
                        .setRefreshTokenId(refreshToken.getRefreshTokenId())
                        .setRefreshToken(refreshToken.getRefreshToken())
                        .setExpiresAt(refreshToken.getExpiresAt().toString())
                        .setUserId(refreshToken.getUser().getUserId())
                        .setStatus(GrpcRefreshTokenStatus.forNumber(refreshToken.getStatus().getNumber()))
                        .setCreatedAt(refreshToken.getCreatedAt().toString())
                        .setUpdatedAt(refreshToken.getUpdatedAt().toString())
                        .build())
                .build();

        ServiceHelper.next(responseObserver, ServiceHelper.packedSuccessResponse(response));
    }

    @Override
    public void updateRefreshTokenByRefreshTokenId(GrpcRequest request, StreamObserver<GrpcResponse> responseObserver) {
        GrpcUpdateRefreshTokenByRefreshTokenIdRequest req = ServiceHelper.unpackedRequest(request, GrpcUpdateRefreshTokenByRefreshTokenIdRequest.class);
        Optional<RefreshToken> updateRefreshTokenOptional = this.refreshTokenRepository.findById(req.getRefreshTokenId());

        if (!updateRefreshTokenOptional.isPresent()) {
            ServiceHelper.next(responseObserver, ServiceHelper.packedErrorResponse(GrpcErrorCode.ERROR_CODE_NOT_FOUND, "refreshToken not found with refreshToken"));
            return;
        }

        RefreshToken updateRefreshToken = updateRefreshTokenOptional.get();

        updateRefreshToken.setStatus(RefreshTokenStatus.forNumber(req.getStatus().getNumber()));

        RefreshToken refreshToken = this.refreshTokenRepository.save(updateRefreshToken);

        GrpcUpdateRefreshTokenByRefreshTokenIdResponse response = GrpcUpdateRefreshTokenByRefreshTokenIdResponse.newBuilder()
                .setRefreshTokenId(refreshToken.getRefreshTokenId())
                .build();

        ServiceHelper.next(responseObserver, ServiceHelper.packedSuccessResponse(response));
    }

    @Override
    public void deleteRefreshTokenByRefreshToken(GrpcRequest request, StreamObserver<GrpcResponse> responseObserver) {
        GrpcDeleteRefreshTokenByRefreshTokenRequest req = ServiceHelper.unpackedRequest(request, GrpcDeleteRefreshTokenByRefreshTokenRequest.class);

        boolean exists = this.refreshTokenRepository.existsByRefreshToken(req.getRefreshToken());

        GrpcDeleteRefreshTokenByRefreshTokenResponse response = GrpcDeleteRefreshTokenByRefreshTokenResponse.newBuilder()
                .setDeleted(true)
                .build();

        if (!exists) {
            ServiceHelper.next(responseObserver, ServiceHelper.packedSuccessResponse(response));
        }

        this.refreshTokenRepository.deleteByRefreshToken(req.getRefreshToken());

        ServiceHelper.next(responseObserver, ServiceHelper.packedSuccessResponse(response));
    }

    @Override
    public void deleteAllRefreshTokenByUserId(GrpcRequest request, StreamObserver<GrpcResponse> responseObserver) {
        GrpcDeleteAllRefreshTokenByUserIdRequest req = ServiceHelper.unpackedRequest(request, GrpcDeleteAllRefreshTokenByUserIdRequest.class);
        Optional<User> userOptional = this.userRepository.findById(req.getUserId());

        GrpcDeleteAllRefreshTokenByUserIdResponse response = GrpcDeleteAllRefreshTokenByUserIdResponse.newBuilder()
                .setDeleted(true)
                .build();

        if (!userOptional.isPresent()) {
            ServiceHelper.next(responseObserver, ServiceHelper.packedSuccessResponse(response));
        }

        User user = userOptional.get();

        this.refreshTokenRepository.deleteAllByUser(user);

        ServiceHelper.next(responseObserver, ServiceHelper.packedSuccessResponse(response));
    }
}
