package com.vuong.app.service;

import com.vuong.app.doman.User;
import com.vuong.app.doman.VerificationCredential;
import com.vuong.app.event.NotificationEmailEvent;
import com.vuong.app.jpa.query.ServiceHelper;
import com.vuong.app.repository.UserRepository;
import com.vuong.app.repository.VerificationCredentialRepository;
import com.vuong.app.v1.*;
import com.vuong.app.v1.message.GrpcErrorCode;
import com.vuong.app.v1.message.GrpcRequest;
import com.vuong.app.v1.message.GrpcResponse;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.kafka.core.KafkaTemplate;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import javax.swing.text.html.Option;
import javax.transaction.Transactional;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;

@GrpcService
@Transactional
@Slf4j
@RequiredArgsConstructor
public class VerificationCredentialService extends VerificationCredentialServiceGrpc.VerificationCredentialServiceImplBase {

    private final VerificationCredentialRepository verificationCredentialRepository;
    private final UserRepository userRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    private String generateVerificationToken() {
        return UUID.randomUUID().toString();
    }

    private String generateVerificationOtp() {
        Random random = new Random();
        int otp = 100000 + random.nextInt(900000);
        return Integer.toString(otp);
    }

    private String buildMailVerify(VerificationCredential verificationCredential) {
        Context context = new Context();
        context.setVariable("otp", verificationCredential.getVerificationOtp());
        context.setVariable("link", "http://localhost:8080/auth/accountVerification/" + verificationCredential.getVerificationToken());
        context.setVariable("expireDate", verificationCredential.getExpireDate());

        TemplateEngine templateEngine = new TemplateEngine();
        return templateEngine.process("mailVerificationAccountTemplate", context);
    }

    private void sendMailVerify(VerificationCredential verificationCredential) {
        this.kafkaTemplate.send("userTopic", new NotificationEmailEvent("Please Activate your Account",
                verificationCredential.getUser().getEmail(), buildMailVerify(verificationCredential)));
    }

    private VerificationCredential createVerificationCredentialByUserId(User user) {
        Instant now = Instant.now();

        return this.verificationCredentialRepository.save(VerificationCredential.builder()
                .verificationToken(this.generateVerificationToken())
                .verificationOtp(this.generateVerificationOtp())
                .expireDate(now.plus(1, ChronoUnit.DAYS))
                .user(user)
                .build());
    }

    public void sendMailVerifyCreateUser(User user) {
        VerificationCredential verificationCredential = this.createVerificationCredentialByUserId(user);
        this.sendMailVerify(verificationCredential);
    }

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



//        Optional<User> userOptional = this.userRepository.findById(verificationCredential.getUserId());
//
//        if (!userOptional.isPresent()) {
//            builderResponse.setVerified(false);
//            ServiceHelper.next(responseObserver, ServiceHelper.packedSuccessResponse(builderResponse.build()));
//        }

        User user = verificationCredential.getUser();

        user.setVerified(Boolean.TRUE);

        this.userRepository.save(user);
        this.verificationCredentialRepository.deleteById(verificationCredential.getVerificationCredentialId());

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

        User user = verificationCredential.getUser();

        user.setVerified(Boolean.TRUE);

        this.userRepository.save(user);
        this.verificationCredentialRepository.deleteById(verificationCredential.getVerificationCredentialId());

        builderResponse.setVerified(true);
        ServiceHelper.next(responseObserver, ServiceHelper.packedSuccessResponse(builderResponse.build()));
    }

    @Override
    public void reissueVerificationCredentialByUserId(GrpcRequest request, StreamObserver<GrpcResponse> responseObserver) {
        GrpcReissueVerificationCredentialByUserIdRequest req = ServiceHelper.unpackedRequest(request, GrpcReissueVerificationCredentialByUserIdRequest.class);

        Optional<User> userOptional = this.userRepository.findById(req.getUserId());

        GrpcReissueVerificationCredentialByUserIdResponse.Builder builderResponse = GrpcReissueVerificationCredentialByUserIdResponse.newBuilder();

        if (!userOptional.isPresent()) {
            builderResponse.setReissue(false);
            ServiceHelper.next(responseObserver, ServiceHelper.packedSuccessResponse(builderResponse.build()));
        }

        User user = userOptional.get();

        VerificationCredential verificationCredentialUpdate = user.getVerificationCredential();

        Instant now = Instant.now();

        verificationCredentialUpdate.setVerificationToken(this.generateVerificationToken());
        verificationCredentialUpdate.setVerificationOtp(this.generateVerificationOtp());
        verificationCredentialUpdate.setExpireDate(now.plus(1, ChronoUnit.DAYS));

        VerificationCredential verificationCredential = this.verificationCredentialRepository.save(verificationCredentialUpdate);

        // send kafka
        this.sendMailVerify(verificationCredential);

        builderResponse.setReissue(true);

        ServiceHelper.next(responseObserver, ServiceHelper.packedSuccessResponse(builderResponse.build()));
    }

    //    @Override
//    public void deleteVerificationCredentialByUserId(GrpcRequest request, StreamObserver<GrpcResponse> responseObserver) {
//        GrpcDeleteVerificationCredentialByUserIdRequest req = ServiceHelper.unpackedRequest(request, GrpcDeleteVerificationCredentialByUserIdRequest.class);
//
//        this.verificationCredentialRepository.deleteByUserId(req.getUserId());
//
//        GrpcDeleteVerificationCredentialByUserIdResponse response = GrpcDeleteVerificationCredentialByUserIdResponse.newBuilder().setDeleted(true).build();
//
//        ServiceHelper.next(responseObserver, ServiceHelper.packedSuccessResponse(response));
//    }
}
