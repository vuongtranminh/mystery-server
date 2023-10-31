package com.vuong.app.doman;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class VerificationCredential {

    private String verificationCredentialId;
    private String verificationToken;
    private String verificationOtp;
    private String expireDate;

}
