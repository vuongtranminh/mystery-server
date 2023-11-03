package com.vuong.app.redis;

import com.vuong.app.websocket.WebSocketSessionManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.Set;

@Service
@Slf4j
public class RedisMessageListener implements MessageListener {

    private final WebSocketSessionManager webSocketSessionManager;

    public RedisMessageListener() {
        this.webSocketSessionManager = WebSocketSessionManager.getInstance();
    }

    @Override
    public void onMessage(Message message, byte[] pattern) {
        String channel = new String(message.getChannel());
        String body = new String(message.getBody());

        log.info("got the message on redis "+ channel + " and "+ body);
        // send message to channel subcribe
        Set<WebSocketSession> wss = this.webSocketSessionManager.getWebSocketSessionsByChannel(channel);
        wss.forEach(ws -> {
            synchronized (ws) {
                try {
                    ws.sendMessage(new TextMessage(body));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }
}
