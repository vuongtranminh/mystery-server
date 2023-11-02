package com.vuong.app.redis.repository.impl;

import com.vuong.app.config.AppProperties;
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
    private final AppProperties appProperties;

    private static final int MAX_USER_AGENT = 2;

    private static final String MANAGER_AUTH_SESSION_KEY_PREFIX = "manage_auth_session:";
    private static final String ACCESS_TOKEN_KEY_PREFIX = "access_token:";
    private static final String REFRESH_TOKEN_KEY_PREFIX = "refresh_token:";

    @Override
    public void storeToken(TokenStore input, AuthMetadata authMetadata) {
        // validate accessToken and refreshToken
        if (input.getAccessToken() == null || input.getRefreshToken() == null) {
            throw new RuntimeException("accessToken and refreshToken not be null");
        }

        TokenStore tokenStore = null;

        String key = MANAGER_AUTH_SESSION_KEY_PREFIX + authMetadata.getUserId();

        if (template.opsForHash().hasKey(key, authMetadata.getUserAgent())) {
            // get current token store
            tokenStore = (TokenStore) template.opsForHash().get(key, authMetadata.getUserAgent());

            // remove old accessToken
            template.opsForValue().getOperations().delete(ACCESS_TOKEN_KEY_PREFIX + tokenStore.getAccessToken());

            // get time to live old refreshToken
            long expire = template.getExpire(REFRESH_TOKEN_KEY_PREFIX + tokenStore.getRefreshToken(), TimeUnit.SECONDS);

            // get first login authMetadata refreshToken
            AuthMetadata firstAuthMetadata = (AuthMetadata) template.opsForValue().get(REFRESH_TOKEN_KEY_PREFIX + tokenStore.getRefreshToken());

            // remove old refreshToken
            template.opsForValue().getOperations().delete(REFRESH_TOKEN_KEY_PREFIX + tokenStore.getRefreshToken());

            // add new refreshToken with expire = time to live of refreshToken
            template.opsForValue().set(REFRESH_TOKEN_KEY_PREFIX + input.getRefreshToken(), firstAuthMetadata);

            // set expire = time to live of old refreshToken
            template.expire(REFRESH_TOKEN_KEY_PREFIX + input.getRefreshToken(), expire, TimeUnit.SECONDS);

            // replace new accesstoken
            tokenStore.setAccessToken(input.getAccessToken());

            // replace new refreshToken
            tokenStore.setRefreshToken(input.getRefreshToken());
        } else {
            // notify for user login other

            // check max UserAgent
            if (template.opsForHash().size(key) == MAX_USER_AGENT) {
                // throw EX max UserAgent
                throw new RuntimeException("max device login: " + MAX_USER_AGENT);
            }

            // add new refreshToken
            template.opsForValue().set(REFRESH_TOKEN_KEY_PREFIX + input.getRefreshToken(), authMetadata);
            // setExprice for new accessToken
            template.expire(REFRESH_TOKEN_KEY_PREFIX + input.getRefreshToken(), appProperties.getAuth().getRefreshTokenExpirationMsec(), TimeUnit.SECONDS);

            tokenStore = TokenStore.builder()
                    .accessToken(input.getAccessToken())
                    .refreshToken(input.getRefreshToken())
                    .build();
        }

        // add new accessToken
        template.opsForValue().set(ACCESS_TOKEN_KEY_PREFIX + input.getAccessToken(), authMetadata);
        // setExprice for new accessToken
        template.expire(ACCESS_TOKEN_KEY_PREFIX + input.getAccessToken(), appProperties.getAuth().getAccessTokenExpirationMsec(), TimeUnit.SECONDS);

        template.opsForHash().put(key, authMetadata.getUserAgent(), tokenStore);
    }

    @Override
    public void removeTokenByAuthMetadata(AuthMetadata authMetadata) {
        String key = MANAGER_AUTH_SESSION_KEY_PREFIX + authMetadata.getUserId();

        if (template.opsForHash().hasKey(key, authMetadata.getUserAgent())) {
            return;
        }

        TokenStore tokenStore = (TokenStore) template.opsForHash().get(key, authMetadata.getUserAgent());

        // remove accessToken
        template.opsForValue().getOperations().delete(ACCESS_TOKEN_KEY_PREFIX + tokenStore.getAccessToken());

        // remove refreshToken
        template.opsForValue().getOperations().delete(REFRESH_TOKEN_KEY_PREFIX + tokenStore.getRefreshToken());

        // remove tokenStore
        template.opsForHash().delete(key, authMetadata.getUserAgent());
    }

    @Override
    public boolean hasAccessToken(String accessToken) {
        return template.opsForValue().getOperations().hasKey(ACCESS_TOKEN_KEY_PREFIX + accessToken);
    }

    @Override
    public boolean hasRefreshToken(String refreshToken) {
        return template.opsForValue().getOperations().hasKey(REFRESH_TOKEN_KEY_PREFIX + refreshToken);
    }

}
