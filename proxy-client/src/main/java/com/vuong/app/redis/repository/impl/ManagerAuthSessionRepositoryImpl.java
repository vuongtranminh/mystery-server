package com.vuong.app.redis.repository.impl;

import com.vuong.app.redis.doman.AuthMetadata;
import com.vuong.app.redis.repository.ManagerAuthSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class ManagerAuthSessionRepositoryImpl implements ManagerAuthSessionRepository {

    private final RedisTemplate<String, Map<String, AuthMetadata>> template;

    private static final String STRING_KEY_PREFIX = "manager-auth-session:";

    @Override
    public void addAccessToken(String userId, String accessToken) {
        String key = STRING_KEY_PREFIX + userId;
        List<String> accessTokens = new ArrayList<>();
        if (template.hasKey(key)) {
            accessTokens = template.opsForValue().get(key);
        }
        accessTokens.add(accessToken);
        template.opsForValue().set(key, accessTokens);
    }

    @Override
    public void removeAllAccessToken(String userId) {
        String key = STRING_KEY_PREFIX + userId;
        template.opsForValue().getOperations().delete(key);
    }

    @Override
    public void removeAccessToken(String userId, String accessToken) {
        String key = STRING_KEY_PREFIX + userId;
        List<String> accessTokens = template.opsForValue().get(key);
        accessTokens.remove(accessToken);
        if (accessTokens.size() == 0) {
            template.opsForValue().getOperations().delete(key);
        } else {
            template.opsForValue().set(key, accessTokens);
        }
    }
}
