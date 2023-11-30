package com.vuong.app.redis;

import com.google.protobuf.Message;
import com.vuong.app.redis.serializer.ProtobufSerializer;
import com.vuong.app.v1.event.GrpcEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class RedisMessagePublisher implements MessagePublisher {

    private final RedisTemplate redisTemplate;
    private final ProtobufSerializer<GrpcEvent> serializer = new ProtobufSerializer<>(GrpcEvent.class);

    @Override
    public void publish(String channel, Message message) {
        log.info("Public {}: {}", channel, message);
        redisTemplate.setValueSerializer(serializer);
        redisTemplate.convertAndSend(channel, message);
    }
}
