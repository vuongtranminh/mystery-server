package com.vuong.app.redis.repository;

import com.vuong.app.redis.doman.AuthMetadata;

import java.util.concurrent.TimeUnit;

public interface AccessTokenRepository {
    void saveAccessToken(String accessToken, AuthMetadata authMetadata, long timeout, TimeUnit unit);
    void deleteAccessToken(String accessToken);
    AuthMetadata getAuthMetadataByAccessToken(String accessToken);
}
