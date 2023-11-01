package com.vuong.app.redis.repository.impl;

import com.vuong.app.redis.doman.AuthMetadata;
import com.vuong.app.redis.repository.AccessTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class AccessTokenRepositoryImpl implements AccessTokenRepository {

    private final RedisTemplate<String, AuthMetadata> template;

    private static final String STRING_KEY_PREFIX = "access-token:";

    @Override
    public void saveAccessToken(String accessToken, AuthMetadata authMetadata, long timeout, TimeUnit unit) {
        template.opsForValue().set(STRING_KEY_PREFIX + accessToken, authMetadata, timeout, unit);
    }

    @Override
    public void deleteAccessToken(String accessToken) {
        template.opsForValue().getOperations().delete(STRING_KEY_PREFIX + accessToken);
    }

    @Override
    public AuthMetadata getAuthMetadataByAccessToken(String accessToken) {
        return template.opsForValue().get(STRING_KEY_PREFIX + accessToken);
    }
}
