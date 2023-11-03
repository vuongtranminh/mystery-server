package com.vuong.app.redis;

import com.vuong.app.v1.discord.GrpcMessageEvent;

public interface MessagePublisher {
    void publish(String channel, GrpcMessageEvent message);
}
