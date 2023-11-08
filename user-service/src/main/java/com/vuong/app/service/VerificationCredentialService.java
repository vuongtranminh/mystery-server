package com.vuong.app.service;

import com.vuong.app.config.MysteryJdbc;
import com.vuong.app.doman.VerificationCredential;
import com.vuong.app.event.NotificationEmailEvent;
import com.vuong.app.v1.*;
import com.vuong.app.v1.user.*;
import io.grpc.Metadata;
import io.grpc.Status;
import io.grpc.protobuf.ProtoUtils;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.kafka.core.KafkaTemplate;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Random;
import java.util.UUID;

@GrpcService
@Slf4j
@RequiredArgsConstructor
public class VerificationCredentialService extends VerificationCredentialServiceGrpc.VerificationCredentialServiceImplBase {

    private final MysteryJdbc mysteryJdbc;

    public String generateVerificationToken() {
        return UUID.randomUUID().toString();
    }

    public String generateVerificationOtp() {
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

    public void sendMailVerify(String email, VerificationCredential verificationCredential) {
//        this.kafkaTemplate.send("userTopic", new NotificationEmailEvent("Please Activate your Account",
//                email, buildMailVerify(verificationCredential)));
    }

    @Override
    public void verificationCredentialByVerificationToken(GrpcVerificationCredentialByVerificationTokenRequest request, StreamObserver<GrpcVerificationCredentialByVerificationTokenResponse> responseObserver) {
        String verificationCredentialQuery = "select " +
                "tbl_verification_credential.id, tbl_verification_credential.verification_token, tbl_verification_credential.verification_otp, tbl_verification_credential.expire_date " +
                "where tbl_verification_credential.verification_token = ?";

        String verifiedUserQuery = "update tbl_user set verified = ? where tbl_user.id = ?";

        Connection con = null;
        PreparedStatement pst1 = null;
        PreparedStatement pst2 = null;
        ResultSet rs = null;

        try {
            con = mysteryJdbc.getConnection();

            pst1 = con.prepareStatement(verificationCredentialQuery);
            pst1.setString(1, request.getVerificationToken());

            rs = pst1.executeQuery();

            if (!rs.next()) {
                Metadata metadata = new Metadata();
                Metadata.Key<GrpcErrorResponse> responseKey = ProtoUtils.keyForProto(GrpcErrorResponse.getDefaultInstance());
                GrpcErrorCode errorCode = GrpcErrorCode.ERROR_CODE_NOT_FOUND;
                GrpcErrorResponse errorResponse = GrpcErrorResponse.newBuilder()
                        .setErrorCode(errorCode)
                        .setMessage("not found with VerificationToken")
                        .build();
                // pass the error object via metadata
                metadata.put(responseKey, errorResponse);
                responseObserver.onError(Status.NOT_FOUND.asRuntimeException(metadata));
                return;
            }

            VerificationCredential verificationCredential = null;

            while (rs.next()) {
                verificationCredential = VerificationCredential.builder()
                        .verificationCredentialId(rs.getString(1))
                        .verificationToken(rs.getString(2))
                        .verificationOtp(rs.getString(3))
                        .expireDate(rs.getString(4))
                        .build();
            }

            if (Instant.parse(verificationCredential.getExpireDate()).isAfter(Instant.now())) {
                Metadata metadata = new Metadata();
                Metadata.Key<GrpcErrorResponse> responseKey = ProtoUtils.keyForProto(GrpcErrorResponse.getDefaultInstance());
                GrpcErrorCode errorCode = GrpcErrorCode.ERROR_CODE_NOT_FOUND;
                GrpcErrorResponse errorResponse = GrpcErrorResponse.newBuilder()
                        .setErrorCode(errorCode)
                        .setMessage("ExpireDate with VerificationToken")
                        .build();
                // pass the error object via metadata
                metadata.put(responseKey, errorResponse);
                responseObserver.onError(Status.NOT_FOUND.asRuntimeException(metadata));
                return;
            }

            pst2 = con.prepareStatement(verifiedUserQuery);
            pst2.setBoolean(1, true);
            pst2.setString(2, verificationCredential.getVerificationCredentialId());

            int result = pst2.executeUpdate();
            GrpcVerificationCredentialByVerificationTokenResponse response = GrpcVerificationCredentialByVerificationTokenResponse.newBuilder()
                    .setVerified(true)
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (SQLException ex) {
            mysteryJdbc.doRollback();
        } finally {
            mysteryJdbc.closeResultSet(rs);
            mysteryJdbc.closePreparedStatement(pst1, pst2);
        }
    }

    @Override
    public void verificationCredentialByVerificationOtp(GrpcVerificationCredentialByVerificationOtpRequest request, StreamObserver<GrpcVerificationCredentialByVerificationOtpResponse> responseObserver) {
        String verificationCredentialQuery = "select " +
                "tbl_verification_credential.id, tbl_verification_credential.verification_token, tbl_verification_credential.verification_otp, tbl_verification_credential.expire_date " +
                "where tbl_verification_credential.verification_token = ?";

        String verifiedUserQuery = "update tbl_user set verified = ? where tbl_user.id = ?";

        Connection con = null;
        PreparedStatement pst1 = null;
        PreparedStatement pst2 = null;
        ResultSet rs = null;

        try {
            con = mysteryJdbc.getConnection();

            pst1 = con.prepareStatement(verificationCredentialQuery);
            pst1.setString(1, request.getVerificationOtp());

            rs = pst1.executeQuery();

            if (!rs.next()) {
                Metadata metadata = new Metadata();
                Metadata.Key<GrpcErrorResponse> responseKey = ProtoUtils.keyForProto(GrpcErrorResponse.getDefaultInstance());
                GrpcErrorCode errorCode = GrpcErrorCode.ERROR_CODE_NOT_FOUND;
                GrpcErrorResponse errorResponse = GrpcErrorResponse.newBuilder()
                        .setErrorCode(errorCode)
                        .setMessage("not found with VerificationToken")
                        .build();
                // pass the error object via metadata
                metadata.put(responseKey, errorResponse);
                responseObserver.onError(Status.NOT_FOUND.asRuntimeException(metadata));
                return;
            }

            VerificationCredential verificationCredential = null;

            while (rs.next()) {
                verificationCredential = VerificationCredential.builder()
                        .verificationCredentialId(rs.getString(1))
                        .verificationToken(rs.getString(2))
                        .verificationOtp(rs.getString(3))
                        .expireDate(rs.getString(4))
                        .build();
            }

            if (Instant.parse(verificationCredential.getExpireDate()).isAfter(Instant.now())) {
                Metadata metadata = new Metadata();
                Metadata.Key<GrpcErrorResponse> responseKey = ProtoUtils.keyForProto(GrpcErrorResponse.getDefaultInstance());
                GrpcErrorCode errorCode = GrpcErrorCode.ERROR_CODE_NOT_FOUND;
                GrpcErrorResponse errorResponse = GrpcErrorResponse.newBuilder()
                        .setErrorCode(errorCode)
                        .setMessage("ExpireDate with VerificationToken")
                        .build();
                // pass the error object via metadata
                metadata.put(responseKey, errorResponse);
                responseObserver.onError(Status.NOT_FOUND.asRuntimeException(metadata));
                return;
            }

            pst2 = con.prepareStatement(verifiedUserQuery);
            pst2.setBoolean(1, true);
            pst2.setString(2, verificationCredential.getVerificationCredentialId());

            int result = pst2.executeUpdate();

            GrpcVerificationCredentialByVerificationOtpResponse response = GrpcVerificationCredentialByVerificationOtpResponse.newBuilder()
                    .setVerified(true)
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (SQLException ex) {
            mysteryJdbc.doRollback();
        } finally {
            mysteryJdbc.closeResultSet(rs);
            mysteryJdbc.closePreparedStatement(pst1, pst2);
        }
    }

    @Override
    public void reissueVerificationCredentialByUserId(GrpcReissueVerificationCredentialByUserIdRequest request, StreamObserver<GrpcReissueVerificationCredentialByUserIdResponse> responseObserver) {
        String userVerificationCredentialQuery = "select " +
                "tbl_user.email, tbl_verification_credential.id " +
                "from tbl_user" +
                "inner join tbl_verification_credential " +
                "ont tbl_user.id = tbl_verification_credential.id " +
                "where tbl_user.id = ?";

        String updateVerificationCredentialQuery = "update tbl_verification_credential set verification_token = ?, verification_otp = ?, expire_date = ? where tbl_verification_credential.id = ?";

        Connection con = null;
        PreparedStatement pst1 = null;
        PreparedStatement pst2 = null;
        ResultSet rs = null;

        try {
            con = mysteryJdbc.getConnection();
            pst1 = con.prepareStatement(updateVerificationCredentialQuery);
            pst1.setString(1, request.getUserId());

            rs = pst1.executeQuery();

            String email = null;
            String verificationCredentialId = null;

            while (rs.next()) {
                email = rs.getString(1);
                verificationCredentialId = rs.getString(2);
            }

            if (verificationCredentialId == null) {
                Metadata metadata = new Metadata();
                Metadata.Key<GrpcErrorResponse> responseKey = ProtoUtils.keyForProto(GrpcErrorResponse.getDefaultInstance());
                GrpcErrorCode errorCode = GrpcErrorCode.ERROR_CODE_NOT_FOUND;
                GrpcErrorResponse errorResponse = GrpcErrorResponse.newBuilder()
                        .setErrorCode(errorCode)
                        .setMessage("not found with VerificationToken")
                        .build();
                // pass the error object via metadata
                metadata.put(responseKey, errorResponse);
                responseObserver.onError(Status.NOT_FOUND.asRuntimeException(metadata));
                return;
            }

            Instant now = Instant.now();
            String verificationToken = this.generateVerificationToken();
            String verificationOtp = this.generateVerificationOtp();
            String expireDate = now.plus(1, ChronoUnit.DAYS).toString();

            pst2 = con.prepareStatement(updateVerificationCredentialQuery);
            pst2.setString(1, verificationToken);
            pst2.setString(2, verificationOtp);
            pst2.setString(3, expireDate);
            pst2.setString(4, verificationCredentialId);

            int result = pst2.executeUpdate();

            VerificationCredential verificationCredential = VerificationCredential.builder()
                    .verificationCredentialId(verificationCredentialId)
                    .verificationToken(verificationToken)
                    .verificationOtp(verificationOtp)
                    .expireDate(expireDate)
                    .build();

            this.sendMailVerify(email, verificationCredential);

            GrpcReissueVerificationCredentialByUserIdResponse response = GrpcReissueVerificationCredentialByUserIdResponse.newBuilder()
                    .setReissue(true)
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (SQLException ex) {
            mysteryJdbc.doRollback();
        } finally {
            mysteryJdbc.closeResultSet(rs);
            mysteryJdbc.closePreparedStatement(pst1, pst2);
        }
    }

}
