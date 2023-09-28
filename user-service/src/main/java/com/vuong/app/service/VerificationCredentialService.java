package com.vuong.app.service;

import com.vuong.app.doman.User;
import com.vuong.app.doman.VerificationCredential;
import com.vuong.app.jpa.query.ServiceHelper;
import com.vuong.app.repository.UserRepository;
import com.vuong.app.repository.VerificationCredentialRepository;
import com.vuong.app.v1.*;
import com.vuong.app.v1.message.GrpcRequest;
import com.vuong.app.v1.message.GrpcResponse;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;

import javax.swing.text.html.Option;
import javax.transaction.Transactional;
import java.time.Instant;
import java.util.Optional;

@GrpcService
@Transactional
@Slf4j
@RequiredArgsConstructor
public class VerificationCredentialService extends VerificationCredentialServiceGrpc.VerificationCredentialServiceImplBase {

    private final VerificationCredentialRepository verificationCredentialRepository;
    private final UserRepository userRepository;

    @Override
    public void verificationCredentialByVerificationToken(GrpcRequest request, StreamObserver<GrpcResponse> responseObserver) {
        GrpcVerificationCredentialByVerificationTokenRequest req = ServiceHelper.unpackedRequest(request, GrpcVerificationCredentialByVerificationTokenRequest.class);

        Optional<VerificationCredential> verificationCredentialOptional = this.verificationCredentialRepository.findByVerificationToken(req.getVerificationToken());

        GrpcVerificationCredentialByVerificationTokenResponse.Builder builderResponse = GrpcVerificationCredentialByVerificationTokenResponse.newBuilder();

        if (!verificationCredentialOptional.isPresent()) {
            builderResponse.setVerified(false);
            ServiceHelper.next(responseObserver, ServiceHelper.packedSuccessResponse(builderResponse.build()));
        }

        VerificationCredential verificationCredential = verificationCredentialOptional.get();

        if (verificationCredential.getExpireDate().isAfter(Instant.now())) {
            builderResponse.setVerified(false);
            ServiceHelper.next(responseObserver, ServiceHelper.packedSuccessResponse(builderResponse.build()));
        }

        Optional<User> userOptional = this.userRepository.findById(verificationCredential.getUserId());

        if (!userOptional.isPresent()) {
            builderResponse.setVerified(false);
            ServiceHelper.next(responseObserver, ServiceHelper.packedSuccessResponse(builderResponse.build()));
        }

        User user = userOptional.get();

        user.setVerified(Boolean.TRUE);

        this.userRepository.save(user);

        builderResponse.setVerified(true);
        ServiceHelper.next(responseObserver, ServiceHelper.packedSuccessResponse(builderResponse.build()));
    }

    @Override
    public void verificationCredentialByVerificationOtp(GrpcRequest request, StreamObserver<GrpcResponse> responseObserver) {
        GrpcVerificationCredentialByVerificationOtpRequest req = ServiceHelper.unpackedRequest(request, GrpcVerificationCredentialByVerificationOtpRequest.class);

        Optional<VerificationCredential> verificationCredentialOptional = this.verificationCredentialRepository.findByVerificationOtp(req.getVerificationOtp());

        GrpcVerificationCredentialByVerificationOtpResponse.Builder builderResponse = GrpcVerificationCredentialByVerificationOtpResponse.newBuilder();

        if (!verificationCredentialOptional.isPresent()) {
            builderResponse.setVerified(false);
            ServiceHelper.next(responseObserver, ServiceHelper.packedSuccessResponse(builderResponse.build()));
        }

        VerificationCredential verificationCredential = verificationCredentialOptional.get();

        if (verificationCredential.getExpireDate().isAfter(Instant.now())) {
            builderResponse.setVerified(false);
            ServiceHelper.next(responseObserver, ServiceHelper.packedSuccessResponse(builderResponse.build()));
        }

        Optional<User> userOptional = this.userRepository.findById(verificationCredential.getUserId());

        if (!userOptional.isPresent()) {
            builderResponse.setVerified(false);
            ServiceHelper.next(responseObserver, ServiceHelper.packedSuccessResponse(builderResponse.build()));
        }

        User user = userOptional.get();

        user.setVerified(Boolean.TRUE);

        this.userRepository.save(user);

        builderResponse.setVerified(true);
        ServiceHelper.next(responseObserver, ServiceHelper.packedSuccessResponse(builderResponse.build()));
    }

    @Override
    public void deleteVerificationCredentialByUserId(GrpcRequest request, StreamObserver<GrpcResponse> responseObserver) {
        GrpcDeleteVerificationCredentialByUserIdRequest req = ServiceHelper.unpackedRequest(request, GrpcDeleteVerificationCredentialByUserIdRequest.class);

        this.verificationCredentialRepository.deleteByUserId(req.getUserId());

        GrpcDeleteVerificationCredentialByUserIdResponse response = GrpcDeleteVerificationCredentialByUserIdResponse.newBuilder().setDeleted(true).build();

        ServiceHelper.next(responseObserver, ServiceHelper.packedSuccessResponse(response));
    }
}
