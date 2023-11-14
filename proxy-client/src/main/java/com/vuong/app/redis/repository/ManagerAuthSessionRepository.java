package com.vuong.app.redis.repository;

import com.vuong.app.redis.doman.AuthMetadata;
import com.vuong.app.redis.doman.TokenStore;

import java.util.List;

public interface ManagerAuthSessionRepository {
    void storeToken(TokenStore input, AuthMetadata authMetadata);
    void removeTokenByAuthMetadata(String userId, AuthMetadata authMetadata);
    boolean hasAccessToken(String accessToken);
    boolean hasRefreshToken(String refreshToken);
    List<AuthMetadata> getAuthMetaData(String userId);
    void removeAuthMetaData(String userId, String userAgent);
}
