package com.vuong.app.redis.repository.impl;

import com.vuong.app.redis.repository.UserRepository;
import com.vuong.app.v1.auth.GrpcUserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepository {

    private final RedisTemplate<String, GrpcUserPrincipal> template;

    private static final String STRING_KEY_PREFIX = "user-service:";


    @Override
    public GrpcUserPrincipal getUserByUserId(String userId) {
        String key = STRING_KEY_PREFIX + userId;
        GrpcUserPrincipal user = template.opsForValue().get(key);

        if (user == null) {
            return null;
        }

        return user;
    }
}
