package com.vuong.app.redis;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.Topic;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class RedisMessageSubscriber {
    private final RedisMessageListenerContainer redisMessageListenerContainer;
    private final MessageListenerAdapter listener;

    public void subscribe(String channel) {
        this.redisMessageListenerContainer.addMessageListener(listener, PatternTopic.of(channel));
    }

    public void unsubscribe(String channel) {
        this.redisMessageListenerContainer.removeMessageListener(listener, PatternTopic.of(channel));
    }

    public void subscribe(Collection<? extends String> channels) {
        Set<Topic> topics = channels.stream().map(channel -> PatternTopic.of(channel)).collect(Collectors.toUnmodifiableSet());
        this.redisMessageListenerContainer.addMessageListener(listener, topics);
    }

    public void unsubscribe(Collection<? extends String> channels) {
        Set<Topic> topics = channels.stream().map(channel -> PatternTopic.of(channel)).collect(Collectors.toUnmodifiableSet());
        this.redisMessageListenerContainer.removeMessageListener(listener, topics);
    }

}
