package com.vuong.app.websocket;

import com.vuong.app.redis.RedisMessageSubscriber;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.List;

@Service
@Slf4j
public class SocketTextHandler extends TextWebSocketHandler {

    private final WebSocketSessionManager webSocketSessionManager;
    private final RedisMessageSubscriber subscriber;

    public SocketTextHandler(RedisMessageSubscriber subscriber) {
        this.webSocketSessionManager = WebSocketSessionManager.getInstance();
        this.subscriber = subscriber;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        this.webSocketSessionManager.addWebSocketSession(session);
        String userId = WebSocketHelper.getUserIdFromSessionAttribute(session);
        List<String> serversId = List.of("b8ae3f8e-3931-49f8-8982-df057c68eeab");
        serversId.forEach(serverId -> {
            this.webSocketSessionManager.addWebSocketSessionToChannel(serverId, session);
        });
        this.subscriber.subscribe(serversId);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        this.webSocketSessionManager.removeWebSocketSession(session);
        String userId = WebSocketHelper.getUserIdFromSessionAttribute(session);
        List<String> serversId = List.of("b8ae3f8e-3931-49f8-8982-df057c68eeab");
        serversId.forEach(serverId -> {
            this.webSocketSessionManager.removeWebSocketSessionToChannel(serverId, session);
        });
        this.subscriber.unsubscribe(serversId);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws IOException {
        if (message.getPayload().equals("ping")) {
            session.sendMessage(new TextMessage("pong"));
        }
//        String payload = message.getPayload();
//        String[] payLoadSplit = payload.split("->");
//        String targetUserId  = payLoadSplit[0].trim();
//        String messageToBeSent = payLoadSplit[1].trim();
//        String userId = WebSocketHelper.getUserIdFromSessionAttribute(session);
//        log.info("got the payload {} and going to send to channel {}", payload, targetUserId);
//        this.redisMessagePublisher.publish(targetUserId, userId + ":" + messageToBeSent);
    }
}
