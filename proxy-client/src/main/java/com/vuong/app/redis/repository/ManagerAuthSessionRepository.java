package com.vuong.app.redis.repository;

public interface ManagerAuthSessionRepository {
    void addAccessToken(String userId, String accessToken);
    void removeAllAccessToken(String userId);
    void removeAccessToken(String userId, String accessToken);
}
