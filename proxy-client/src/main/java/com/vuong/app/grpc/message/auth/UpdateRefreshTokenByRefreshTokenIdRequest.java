package com.vuong.app.grpc.message.auth;

import com.vuong.app.business.auth.model.RefreshTokenStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class UpdateRefreshTokenByRefreshTokenIdRequest {
    private String refreshTokenId;
    private RefreshTokenStatus status;
}
