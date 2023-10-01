package com.vuong.app.grpc.message.auth;

import com.vuong.app.business.auth.model.RefreshTokenStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class CreateRefreshTokenRequest {
    private String refreshToken;
    private Instant expiresAt;
    private Integer userId;
}
