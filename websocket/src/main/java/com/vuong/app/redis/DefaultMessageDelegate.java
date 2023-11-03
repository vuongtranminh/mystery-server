package com.vuong.app.redis;

import com.vuong.app.websocket.WebSocketSessionManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.io.Serializable;
import java.util.Map;

@Service
@Slf4j
public class DefaultMessageDelegate implements MessageDelegate {

    private final WebSocketSessionManager webSocketSessionManager;

    public DefaultMessageDelegate() {
        this.webSocketSessionManager = WebSocketSessionManager.getInstance();
    }


    @Override
    public void handleMessage(String message) {
        log.info("got the message on redis"+ message);
    }

    @Override
    public void handleMessage(Map message) {
        log.info("got the message on redis"+ message);
    }

    @Override
    public void handleMessage(byte[] message) {
        log.info("got the message on redis"+ message);
    }

    @Override
    public void handleMessage(Serializable message) {
        log.info("got the message on redis"+ message);
    }

    @Override
    public void handleMessage(Serializable message, String channel) {
        log.info("got the message on redis "+ channel + " and "+ message);
        WebSocketSession ws = this.webSocketSessionManager.getWebSocketSessions(channel);
        try {
            ws.sendMessage(new TextMessage(message.toString()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
