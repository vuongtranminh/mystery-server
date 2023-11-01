package com.vuong.app.redis.repository;

import com.vuong.app.v1.auth.GrpcUserPrincipal;

public interface UserRepository {
    GrpcUserPrincipal getUserByUserId(String userId);
}
