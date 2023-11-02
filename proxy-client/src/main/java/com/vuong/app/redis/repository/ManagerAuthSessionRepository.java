package com.vuong.app.redis.repository;

import com.vuong.app.redis.doman.AuthMetadata;
import com.vuong.app.redis.doman.TokenStore;

public interface ManagerAuthSessionRepository {
    void storeToken(TokenStore input, AuthMetadata authMetadata);
    void removeTokenByAuthMetadata(AuthMetadata authMetadata);
    boolean hasAccessToken(String accessToken);
    boolean hasRefreshToken(String refreshToken);
}
