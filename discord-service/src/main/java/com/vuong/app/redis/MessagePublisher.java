package com.vuong.app.redis;

import com.google.protobuf.Message;

public interface MessagePublisher {
    void publish(String channel, Message message);
}
