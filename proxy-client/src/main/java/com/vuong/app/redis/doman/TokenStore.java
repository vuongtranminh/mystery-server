package com.vuong.app.redis.doman;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class TokenStore implements Serializable {
    private String accessToken;
    private String refreshToken;
}
