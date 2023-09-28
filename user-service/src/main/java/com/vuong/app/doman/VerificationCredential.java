package com.vuong.app.doman;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import javax.persistence.*;
import java.time.Instant;

import static com.fasterxml.jackson.annotation.JsonFormat.Shape.STRING;

@Entity
@Table(name = "verification_credentials")
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class VerificationCredential {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "verification_credential_id", unique = true, nullable = false, updatable = false)
    private Integer verificationCredentialId;

    @Column(name = "verification_token", unique = true)
    private String verificationToken;

    @Column(name = "verification_otp", unique = true)
    private String verificationOtp;

    @JsonFormat(shape = STRING)
    @Column(name = "expire_date", nullable = false)
    private Instant expireDate;

    @Column(name = "user_id", unique = true, nullable = false)
    private Integer userId;
}
