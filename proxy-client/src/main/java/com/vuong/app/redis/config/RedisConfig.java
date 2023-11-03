package com.vuong.app.redis.config;

import com.vuong.app.redis.doman.AuthMetadata;
import com.vuong.app.redis.doman.TokenStore;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;

@Configuration
@RequiredArgsConstructor
public class RedisConfig {

    private final RedisProperties redisProperties;

    public static final String REDIS_TEMPALTE_MANAGER_AUTH_SESSION_BEAN = "redisTemplateManagerAuthSession";
    public static final String REDIS_TEMPALTE_ACCESS_TOKEN_BEAN = "redisTemplateAccessToken";
    public static final String REDIS_TEMPALTE_REFRESH_TOKEN_BEAN = "redisTemplateRefreshToken";

    @Bean
    RedisConnectionFactory redisConnectionFactory() {
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration();
        config.setHostName(redisProperties.getHost());
        config.setPort(redisProperties.getPort());
//        config.setPassword(redisProperties.getPassword());
        return new LettuceConnectionFactory(config);
    }

    @Bean
    public RedisTemplate<?, ?> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<?, ?> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        return template;
    }

    @Bean(REDIS_TEMPALTE_MANAGER_AUTH_SESSION_BEAN)
    public RedisTemplate<String, TokenStore> redisTemplateManagerAuthSession(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, TokenStore> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        return template;
    }

    @Bean(REDIS_TEMPALTE_ACCESS_TOKEN_BEAN)
    public RedisTemplate<String, AuthMetadata> redisTemplateAccessToken(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, AuthMetadata> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        return template;
    }

    @Bean(REDIS_TEMPALTE_REFRESH_TOKEN_BEAN)
    public RedisTemplate<String, AuthMetadata> redisTemplateRefreshToken(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, AuthMetadata> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        return template;
    }
}
