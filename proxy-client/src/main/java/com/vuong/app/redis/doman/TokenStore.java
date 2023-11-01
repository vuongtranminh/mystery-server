package com.vuong.app.redis.doman;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class TokenStore {
    private List<String> accessTokens;
    private List<String> refreshTokens;
}
