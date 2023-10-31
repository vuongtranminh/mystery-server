package com.vuong.app.doman;

import lombok.*;
import java.io.Serializable;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public final class RefreshToken extends AbstractMappedEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    private String refreshTokenId;
    private String refreshToken;
    private String expiresAt;
    private String userId;
    private RefreshTokenStatus status;
}
