package com.vuong.app.redis.repository;

import com.vuong.app.v1.auth.GrpcUserPrincipal;

public interface UserRepository {
    void saveUser(GrpcUserPrincipal userPrincipal);
    void deleteUser(String userId);
}
