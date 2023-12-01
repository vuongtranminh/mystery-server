package com.vuong.app.websocket;

import com.vuong.app.grpc.ServerClientService;
import com.vuong.app.redis.RedisMessageSubscriber;
import com.vuong.app.v1.discord.GrpcGetServerJoinByServerIdRequest;
import com.vuong.app.v1.discord.GrpcGetServerJoinByServerIdResponse;
import com.vuong.app.v1.discord.GrpcGetServerJoinIdsRequest;
import com.vuong.app.v1.discord.GrpcGetServerJoinIdsResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.AbstractWebSocketHandler;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@Slf4j
public class SocketTextHandler extends AbstractWebSocketHandler {

    private final WebSocketSessionManager webSocketSessionManager;
    private final RedisMessageSubscriber subscriber;
    private final ServerClientService serverClientService;

    public SocketTextHandler(RedisMessageSubscriber subscriber, ServerClientService serverClientService, WebSocketSessionManager webSocketSessionManager) {
        this.webSocketSessionManager = webSocketSessionManager;
        this.subscriber = subscriber;
        this.serverClientService = serverClientService;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
//        this.webSocketSessionManager.addWebSocketSession(session);
//        String userId = WebSocketHelper.getUserIdFromSessionAttribute(session);
//        GrpcGetServerJoinIdsResponse response = this.serverClientService.getServerJoinIds(GrpcGetServerJoinIdsRequest.newBuilder()
//                        .setProfileId(userId)
//                .build());
//        List<String> serversId = new ArrayList<>();
//        response.getResultList().stream().forEach(serverId -> {
//            this.webSocketSessionManager.addWebSocketSessionToChannel(serverId, session);
//            serversId.add(serverId);
//        });
//        this.webSocketSessionManager.addServerIdsByUserId(userId, serversId);
        String userId = WebSocketHelper.getUserIdFromSessionAttribute(session);
        this.webSocketSessionManager.addWebSocketSession(userId, session);
        GrpcGetServerJoinIdsResponse response = this.serverClientService.getServerJoinIds(GrpcGetServerJoinIdsRequest.newBuilder()
                .setProfileId(userId)
                .build());
        Set<String> serversId = new HashSet<>();
        response.getResultList().stream().forEach(serverId -> {
            this.webSocketSessionManager.addUserIdListenerServer(serverId, userId);
            serversId.add(serverId);
        });
        this.webSocketSessionManager.addServerIds(userId, serversId);
        this.subscriber.subscribe(serversId);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
//        this.webSocketSessionManager.removeWebSocketSession(session);
//        String userId = WebSocketHelper.getUserIdFromSessionAttribute(session);
//
//        List<String> serversId = this.webSocketSessionManager.getServerIdsByUserId(userId);
//        serversId.forEach(serverId -> {
//            this.webSocketSessionManager.removeWebSocketSessionToChannel(serverId, session);
//        });
//        this.webSocketSessionManager.removeServerIdsByUserId(userId);
//        this.subscriber.unsubscribe(serversId);
        String userId = WebSocketHelper.getUserIdFromSessionAttribute(session);
        this.webSocketSessionManager.removeWebSocketSession(userId);
        Set<String> serversId = this.webSocketSessionManager.getServerIds(userId);
        serversId.forEach(serverId -> {
            this.webSocketSessionManager.removeUserIdListenerServer(serverId, userId);
        });
        this.webSocketSessionManager.removeServerIds(userId);
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

    @Override
    protected void handleBinaryMessage(WebSocketSession session, BinaryMessage message) throws Exception {
        super.handleBinaryMessage(session, message);
    }

//    @Override
//    protected void handlePongMessage(WebSocketSession session, PongMessage message) throws Exception {
//        super.handlePongMessage(session, message);
//    }
//
//    @Override
//    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
//        super.handleTransportError(session, exception);
//    }
}
