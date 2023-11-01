package com.vuong.app.redis.repository.impl;

import com.vuong.app.redis.repository.UserRepository;
import com.vuong.app.v1.auth.GrpcUserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepository {

    private final RedisTemplate<String, GrpcUserPrincipal> template;

    private static final String STRING_KEY_PREFIX = "user-service:";

    @Override
    public void saveUser(GrpcUserPrincipal userPrincipal) {
        String key = STRING_KEY_PREFIX + userPrincipal.getUserId();
        this.template.opsForValue().set(key, userPrincipal, 1, TimeUnit.HOURS);
    }

    @Override
    public void deleteUser(String userId) {
        String key = STRING_KEY_PREFIX + userId;
        this.template.opsForValue().getOperations().delete(key);
    }

}
