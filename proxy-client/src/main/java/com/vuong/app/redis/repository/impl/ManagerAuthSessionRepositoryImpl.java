package com.vuong.app.redis.repository.impl;

import com.vuong.app.redis.doman.AuthMetadata;
import com.vuong.app.redis.doman.TokenStore;
import com.vuong.app.redis.repository.ManagerAuthSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ManagerAuthSessionRepositoryImpl implements ManagerAuthSessionRepository {

    private final RedisTemplate template;

    private static final String MANAGER_AUTH_SESSION_KEY_PREFIX = "manage_auth_session:";
    private static final String ACCESS_TOKEN_KEY_PREFIX = "access_token:";
    private static final String REFRESH_TOKEN_KEY_PREFIX = "refresh_token:";

    @Override
    public void storeAccessToken(String accessToken, AuthMetadata authMetadata) {
        String key = MANAGER_AUTH_SESSION_KEY_PREFIX + authMetadata.getUserId();

        if (template.opsForHash().size(key) == 5) {
            // tối đa 5 thiết bị
            return;
        }

        TokenStore tokenStore;

        if (template.opsForHash().hasKey(key, authMetadata.getUserAgent())) {
            tokenStore = (TokenStore) template.opsForHash().get(key, authMetadata);
            List<String> accessTokens = tokenStore.getAccessTokens().stream()
                    .filter(at -> template.opsForValue().getOperations().hasKey(ACCESS_TOKEN_KEY_PREFIX + at))
                    .collect(Collectors.toUnmodifiableList());
            accessTokens.add(accessToken);
            tokenStore.setAccessTokens(accessTokens);
        } else {
            tokenStore = TokenStore.builder()
                    .accessTokens(Arrays.asList(accessToken))
                    .build();
        }

        template.opsForHash().put(key, authMetadata.getUserAgent(), tokenStore);
        template.opsForValue().set(ACCESS_TOKEN_KEY_PREFIX + accessToken, authMetadata, 1, TimeUnit.HOURS);
    }

    @Override
    public void removeAllAccessTokenByUserId(String userId) {
        String key = MANAGER_AUTH_SESSION_KEY_PREFIX + userId;
        template.opsForHash().delete(key);
    }

    @Override
    public void removeAllAccessTokenByUserIdAndUserAgent(String userId, AuthMetadata authMetadata) {
        String key = MANAGER_AUTH_SESSION_KEY_PREFIX + userId;
        template.opsForHash().delete(key, authMetadata.getUserAgent());
    }

    public void removeAccessToken(String accessToken) {
        AuthMetadata authMetadata = (AuthMetadata) template.opsForValue().get(ACCESS_TOKEN_KEY_PREFIX + accessToken);
        String key = MANAGER_AUTH_SESSION_KEY_PREFIX + authMetadata.getUserId();
        template.opsForValue().getOperations().delete(accessToken);

        if (template.opsForHash().hasKey(key, authMetadata.getUserAgent())) {
            TokenStore tokenStore = (TokenStore) template.opsForHash().get(key, authMetadata);
            List<String> accessTokens = tokenStore.getAccessTokens().stream()
                    .filter(at -> template.opsForValue().getOperations().hasKey(ACCESS_TOKEN_KEY_PREFIX + at))
                    .collect(Collectors.toUnmodifiableList());

            if (accessTokens.size() == 0 && tokenStore.getRefreshTokens().size() == 0) {
                template.opsForHash().delete(key, authMetadata.getUserAgent());
            } else {
                tokenStore.setAccessTokens(accessTokens);
                template.opsForHash().put(key, authMetadata.getUserAgent(), tokenStore);
            }
        }
    }

    public boolean hasAccessToken(String accessToken, AuthMetadata authMetadata) {
        String key = MANAGER_AUTH_SESSION_KEY_PREFIX + authMetadata.getUserId();
        if (template.opsForValue().getOperations().hasKey(ACCESS_TOKEN_KEY_PREFIX + accessToken)) {
            return true;
        }

        if (template.opsForHash().hasKey(key, authMetadata.getUserAgent())) {
            TokenStore tokenStore = (TokenStore) template.opsForHash().get(key, authMetadata);
            List<String> accessTokens = tokenStore.getAccessTokens().stream()
                    .filter(at -> template.opsForValue().getOperations().hasKey(ACCESS_TOKEN_KEY_PREFIX + at))
                    .collect(Collectors.toUnmodifiableList());

            if (accessTokens.size() == 0 && tokenStore.getRefreshTokens().size() == 0) {
                template.opsForHash().delete(key, authMetadata.getUserAgent());
            } else {
                tokenStore.setAccessTokens(accessTokens);
                template.opsForHash().put(key, authMetadata.getUserAgent(), tokenStore);
            }
        }

        return false;
    }

    public void readRefreshToken() {

    }

    public void storeRefreshToken() {

    }


}
