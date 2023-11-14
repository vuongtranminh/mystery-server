package com.vuong.app.redis.repository.impl;

import com.vuong.app.config.AppProperties;
import com.vuong.app.redis.config.RedisConfig;
import com.vuong.app.redis.doman.AuthMetadata;
import com.vuong.app.redis.doman.TokenStore;
import com.vuong.app.redis.repository.ManagerAuthSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Component
public class ManagerAuthSessionRepositoryImpl implements ManagerAuthSessionRepository {

    private final RedisTemplate<String, Map<String, TokenStore>> redisTemplateManagerAuthSession; // expire = expire refreshToken
    private final RedisTemplate<String, AuthMetadata> redisTemplateAccessToken; // expire = expire accessToken
    private final RedisTemplate<String, AuthMetadata> redisTemplateRefreshToken; // expire = expire refreshToken

    private final AppProperties appProperties;

    private static final int MAX_USER_AGENT = 2;

    private static final String MANAGER_AUTH_SESSION_KEY_PREFIX = "manage_auth_session:";
    private static final String ACCESS_TOKEN_KEY_PREFIX = "access_token:";
    private static final String REFRESH_TOKEN_KEY_PREFIX = "refresh_token:";

    private ValueOperations<String, Map<String, TokenStore>> managerAuthSessionOperations;
    private ValueOperations<String, AuthMetadata> accessTokenOperations;
    private ValueOperations<String, AuthMetadata> refreshTokenOperations;

    public ManagerAuthSessionRepositoryImpl(@Qualifier(RedisConfig.REDIS_TEMPLATE_MANAGER_AUTH_SESSION_BEAN) RedisTemplate<String, Map<String, TokenStore>> redisTemplateManagerAuthSession,
                                            @Qualifier(RedisConfig.REDIS_TEMPLATE_ACCESS_TOKEN_BEAN) RedisTemplate<String, AuthMetadata> redisTemplateAccessToken,
                                            @Qualifier(RedisConfig.REDIS_TEMPLATE_REFRESH_TOKEN_BEAN) RedisTemplate<String, AuthMetadata> redisTemplateRefreshToken,
                                            AppProperties appProperties) {
        this.redisTemplateManagerAuthSession = redisTemplateManagerAuthSession;
        this.redisTemplateAccessToken = redisTemplateAccessToken;
        this.redisTemplateRefreshToken = redisTemplateRefreshToken;
        this.appProperties = appProperties;

        this.managerAuthSessionOperations = redisTemplateManagerAuthSession.opsForValue();
        this.accessTokenOperations = redisTemplateAccessToken.opsForValue();
        this.refreshTokenOperations = redisTemplateRefreshToken.opsForValue();
    }

    @Override
    public void storeToken(TokenStore input, AuthMetadata authMetadata) {
        // validate accessToken and refreshToken
        if (input.getAccessToken() == null || input.getRefreshToken() == null) {
            throw new RuntimeException("accessToken and refreshToken not be null");
        }

        String key = MANAGER_AUTH_SESSION_KEY_PREFIX + authMetadata.getUserId();

        // has userId and has login in device
        if (managerAuthSessionOperations.getOperations().hasKey(key) && managerAuthSessionOperations.get(key).containsKey(authMetadata.getUserAgent())) {
            Map<String, TokenStore> authInfo = managerAuthSessionOperations.get(key);
            // get curren tokenStore
            TokenStore tokenStore = authInfo.get(authMetadata.getUserAgent());

            // remove old accessToken
            accessTokenOperations.getOperations().delete(ACCESS_TOKEN_KEY_PREFIX + tokenStore.getAccessToken());

            // set new accessToken
            accessTokenOperations.set(ACCESS_TOKEN_KEY_PREFIX + input.getAccessToken(), authMetadata);
            // setExprice for new accessToken
            redisTemplateAccessToken.expire(ACCESS_TOKEN_KEY_PREFIX + input.getAccessToken(), appProperties.getAuth().getAccessTokenExpirationMsec(), TimeUnit.SECONDS);

            // get time to live old refreshToken
            long expire = redisTemplateRefreshToken.getExpire(REFRESH_TOKEN_KEY_PREFIX + tokenStore.getRefreshToken(), TimeUnit.SECONDS);

            // get first login authMetadata refreshToken
            AuthMetadata firstAuthMetadata = refreshTokenOperations.get(REFRESH_TOKEN_KEY_PREFIX + tokenStore.getRefreshToken());

            // remove old refreshToken
            refreshTokenOperations.getOperations().delete(REFRESH_TOKEN_KEY_PREFIX + tokenStore.getRefreshToken());

            // add new refreshToken with expire = time to live of refreshToken
            refreshTokenOperations.set(REFRESH_TOKEN_KEY_PREFIX + input.getRefreshToken(), firstAuthMetadata);

            // set expire = time to live of old refreshToken
            redisTemplateRefreshToken.expire(REFRESH_TOKEN_KEY_PREFIX + input.getRefreshToken(), expire, TimeUnit.SECONDS);

            // replace new accesstoken
            tokenStore.setAccessToken(input.getAccessToken());

            // replace new refreshToken
            tokenStore.setRefreshToken(input.getRefreshToken());

            // replace device with new tokenStore
            authInfo.replace(authMetadata.getUserAgent(), tokenStore);

            // update userInfo with device for new tokenStore
            managerAuthSessionOperations.set(key, authInfo);

            // set expire = time to live of old refreshToken
            redisTemplateManagerAuthSession.expire(key, expire, TimeUnit.SECONDS);
        } else {
            // notify for user login other

            // check max UserAgent
            if (managerAuthSessionOperations.getOperations().hasKey(key) && managerAuthSessionOperations.get(key).size() == MAX_USER_AGENT) {
                // throw EX max UserAgent
                throw new RuntimeException("max device login: " + MAX_USER_AGENT);
            }

            // add new accessToken
            accessTokenOperations.set(ACCESS_TOKEN_KEY_PREFIX + input.getAccessToken(), authMetadata);
            // setExprice for new accessToken
            redisTemplateAccessToken.expire(ACCESS_TOKEN_KEY_PREFIX + input.getAccessToken(), appProperties.getAuth().getAccessTokenExpirationMsec(), TimeUnit.SECONDS);

            // add new refreshToken
            refreshTokenOperations.set(REFRESH_TOKEN_KEY_PREFIX + input.getRefreshToken(), authMetadata);
            // setExprice for new refreshToken
            redisTemplateRefreshToken.expire(REFRESH_TOKEN_KEY_PREFIX + input.getRefreshToken(), appProperties.getAuth().getRefreshTokenExpirationMsec(), TimeUnit.SECONDS);

            TokenStore tokenStore = TokenStore.builder()
                    .accessToken(input.getAccessToken())
                    .refreshToken(input.getRefreshToken())
                    .build();

            Map<String, TokenStore> authInfo = Collections.singletonMap(authMetadata.getUserAgent(), tokenStore);

            // set userInfo with device for new tokenStore
            managerAuthSessionOperations.set(key, authInfo);

            // set expire = time to live of refreshToken
            redisTemplateManagerAuthSession.expire(key, appProperties.getAuth().getRefreshTokenExpirationMsec(), TimeUnit.SECONDS);
        }
    }

    @Override
    public void removeTokenByAuthMetadata(String userId, AuthMetadata authMetadata) {
        String key = MANAGER_AUTH_SESSION_KEY_PREFIX + userId;

        if (!managerAuthSessionOperations.getOperations().hasKey(key) || !managerAuthSessionOperations.get(key).containsKey(authMetadata.getUserAgent())) {
            return;
        }

        Map<String, TokenStore> authInfo = managerAuthSessionOperations.get(key);

        TokenStore tokenStore = authInfo.get(authMetadata.getUserAgent());

        // remove accessToken
        accessTokenOperations.getOperations().delete(ACCESS_TOKEN_KEY_PREFIX + tokenStore.getAccessToken());

        // remove refreshToken
        refreshTokenOperations.getOperations().delete(REFRESH_TOKEN_KEY_PREFIX + tokenStore.getRefreshToken());

        // remove tokenStore
        managerAuthSessionOperations.getOperations().delete(key);
    }

    @Override
    public boolean hasAccessToken(String accessToken) {
        return accessTokenOperations.getOperations().hasKey(ACCESS_TOKEN_KEY_PREFIX + accessToken);
    }

    @Override
    public boolean hasRefreshToken(String refreshToken) {
        return refreshTokenOperations.getOperations().hasKey(REFRESH_TOKEN_KEY_PREFIX + refreshToken);
    }

    @Override
    public List<AuthMetadata> getAuthMetaData(String userId) {
        String key = MANAGER_AUTH_SESSION_KEY_PREFIX + userId;

        if (!managerAuthSessionOperations.getOperations().hasKey(key)) {
            return Collections.emptyList();
        }

        Map<String, TokenStore> authInfo = managerAuthSessionOperations.get(key);

        List<TokenStore> tokenStores = new ArrayList<>(authInfo.values());

        List<AuthMetadata> result = tokenStores.stream()
                .map(tokenStore -> refreshTokenOperations.get(tokenStore.getRefreshToken()))
                .collect(Collectors.toUnmodifiableList());

        return result;
    }

    @Override
    public void removeAuthMetaData(String userId, String userAgent) {
        String key = MANAGER_AUTH_SESSION_KEY_PREFIX + userId;

        if (!managerAuthSessionOperations.getOperations().hasKey(key)) {
            return;
        }

        Map<String, TokenStore> authInfo = managerAuthSessionOperations.get(key);

        authInfo.remove(userAgent);
    }

}
