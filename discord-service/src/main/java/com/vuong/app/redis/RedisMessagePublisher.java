package com.vuong.app.redis;

import com.vuong.app.redis.serializer.ProtobufSerializer;
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
    private final ProtobufSerializer<GrpcMessageEvent> serializer = new ProtobufSerializer<GrpcMessageEvent>(GrpcMessageEvent.class);

    @Override
    public void publish(String channel, GrpcMessageEvent message) {
        log.info("Public {}: {}", channel, message);
        redisTemplate.setValueSerializer(serializer);
        redisTemplate.convertAndSend(channel, message);
    }
}
