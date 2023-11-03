package com.vuong.app.redis;

import com.vuong.app.v1.discord.GrpcMessageEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class RedisMessagePublisher implements MessagePublisher {

    private final RedisTemplate redisTemplate;

    @Override
    public void publish(String channel, GrpcMessageEvent message) {
        log.info("Public {}: {}", channel, message);
        redisTemplate.convertAndSend(channel, message);
    }
}
