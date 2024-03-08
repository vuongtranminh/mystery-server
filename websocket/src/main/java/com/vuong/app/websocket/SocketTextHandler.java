package com.vuong.app.websocket;

import com.google.protobuf.ByteString;
import com.vuong.app.grpc.ServerClientService;
import com.vuong.app.redis.RedisMessageSubscriber;
import com.vuong.app.redis.serializer.ProtobufSerializer;
import com.vuong.app.v1.ClientMsg;
import com.vuong.app.v1.ClientSendMessage;
import com.vuong.app.v1.discord.GrpcGetServerJoinIdsRequest;
import com.vuong.app.v1.discord.GrpcGetServerJoinIdsResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.AbstractWebSocketHandler;

import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
public class SocketTextHandler extends AbstractWebSocketHandler {

    private final WebSocketSessionManager webSocketSessionManager;
    private final RedisMessageSubscriber subscriber;
    private final ServerClientService serverClientService;
    private final ProtobufSerializer<ClientMsg> serializer;

    public SocketTextHandler(RedisMessageSubscriber subscriber, ServerClientService serverClientService, WebSocketSessionManager webSocketSessionManager) {
        this.webSocketSessionManager = webSocketSessionManager;
        this.subscriber = subscriber;
        this.serverClientService = serverClientService;
        this.serializer = new ProtobufSerializer<>(ClientMsg.class);
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        String userId = WebSocketHelper.getUserIdFromSessionAttribute(session);
        GrpcGetServerJoinIdsResponse response = this.serverClientService.getServerJoinIds(GrpcGetServerJoinIdsRequest.newBuilder()
                .setProfileId(userId)
                .build());
        Set<String> serversId = new HashSet<>();
        response.getResultList().stream().forEach(serverId -> {
            this.webSocketSessionManager.addWs(WebSocket.builder()
                    .session(session)
                    .userId(userId)
                    .serverId(serverId)
                    .build());
            serversId.add(serverId);
        });
//        this.subscriber.subscribe(serversId);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
//        String userId = WebSocketHelper.getUserIdFromSessionAttribute(session);
//        log.info("UserIdFromSessionAttribute: {}", userId);
//        this.webSocketSessionManager.removeWebSocketSession(userId, session);
//        Set<String> serversId = this.webSocketSessionManager.getServerIds(userId);
//        serversId.forEach(serverId -> {
//            this.webSocketSessionManager.removeUserIdListenerServer(serverId, userId);
//        });
////        this.webSocketSessionManager.removeServerIds(userId);
        String userId = WebSocketHelper.getUserIdFromSessionAttribute(session);
        log.info("afterConnectionClosed: {}", userId );
        Set<String> serversId = this.webSocketSessionManager.getWebSocketsBySession(session).stream().map(webSocket -> webSocket.getServerId()).collect(Collectors.toUnmodifiableSet());
        this.webSocketSessionManager.removeWsBySession(session);
//        this.subscriber.unsubscribe(serversId);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws IOException {
        if (message.getPayload().equals("ping")) {
            session.sendMessage(new TextMessage("pong"));
        } else {
            log.info("handleTextMessage: {}", message);
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
        log.info("handleBinaryMessage: {}", message);
        ClientMsg clientMsg = serializer.deserialize(message.getPayload().array());
        Map<String, ByteString> head = clientMsg.getHeadMap();
        String token = head.get("token").toStringUtf8();
        log.info("TOKEN: {}", token);
        switch (clientMsg.getMessageCase()) {
            case SEND:
                ClientSendMessage send = clientMsg.getSend();
                String content = send.getContent().toStringUtf8();
                // send to kafka insert to db
                // success to redis send to client
                log.info("content: {}", content);
                break;
            case SEEN:
                break;
            default:
                return;
        }
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
