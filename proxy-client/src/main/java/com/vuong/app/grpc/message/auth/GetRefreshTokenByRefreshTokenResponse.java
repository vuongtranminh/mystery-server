package com.vuong.app.grpc.message.auth;

import com.vuong.app.business.auth.model.RefreshTokenStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class GetRefreshTokenByRefreshTokenResponse {
    private String refreshTokenId;
    private String refreshToken;
    private Date expiresAt;
    private String userId;
    private RefreshTokenStatus status;
}
