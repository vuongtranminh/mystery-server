package com.vuong.app.redis;

import com.vuong.app.redis.serializer.ProtobufSerializer;
import com.vuong.app.v1.event.GrpcEvent;
import com.vuong.app.websocket.WebSocket;
import com.vuong.app.websocket.WebSocketSessionManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.Set;

@Service
@Slf4j
public class RedisMessageListener implements MessageListener {

    private final WebSocketSessionManager webSocketSessionManager;
    private final ProtobufSerializer<GrpcEvent> serializer;

    public RedisMessageListener(WebSocketSessionManager webSocketSessionManager) {
        this.webSocketSessionManager = webSocketSessionManager;
        this.serializer = new ProtobufSerializer<>(GrpcEvent.class);
    }

    @Override
    public void onMessage(Message message, byte[] pattern) {
        String channel = new String(message.getChannel());
//        String body = new String(message.getBody());

//        GrpcMessageEvent grpcEvent = serializer.deserialize(message.getBody());
//        MessageEvent event = MessageEvent.builder()
//                .type(grpcEvent.getType().getNumber() == 1 ? "add" : "update")
//                .message(com.vuong.app.doman.Message.builder()
//                        .messageId(grpcEvent.getMessage().getMessageId())
//                        .content(grpcEvent.getMessage().getContent())
//                        .fileUrl(grpcEvent.getMessage().getFileUrl())
//                        .channelId(grpcEvent.getMessage().getChannelId())
//                        .createdAt(grpcEvent.getMessage().getCreatedAt())
//                        .updatedAt(grpcEvent.getMessage().getUpdatedAt())
//                        .deletedAt(grpcEvent.getMessage().getDeletedAt())
//                        .deletedBy(grpcEvent.getMessage().getDeletedBy())
//                        .author(MemberProfile.builder()
//                                .memberId(grpcEvent.getMessage().getAuthor().getMemberId())
//                                .role(grpcEvent.getMessage().getAuthor().getRole().getNumber())
//                                .serverId(grpcEvent.getMessage().getAuthor().getServerId())
//                                .joinAt(grpcEvent.getMessage().getAuthor().getJoinAt())
//                                .profileId(grpcEvent.getMessage().getAuthor().getProfileId())
//                                .name(grpcEvent.getMessage().getAuthor().getName())
//                                .avtUrl(grpcEvent.getMessage().getAuthor().getAvtUrl())
//                                .build())
//                        .build())
//                .build();
//
//        String body = event.toString();
//
//        log.info("got the message on redis "+ channel + " and "+ body);

//        GrpcEvent grpcEvent = serializer.deserialize(message.getBody());
//        GrpcEvent.EventCase eventCase = grpcEvent.getEventCase();
//
//        byte[] bytes = ByteBuffer.allocate(4).putInt(eventCase.getNumber()).array();
//        byte[] data = ByteBuffer.allocate(bytes.length + message.getBody().length)
//                .put(bytes)
//                .put(message.getBody())
//                .array();
        // send message to channel subcribe

        Set<WebSocket> webSockets = this.webSocketSessionManager.getWebSocketsByServerId(channel);
        webSockets.forEach(webSocket -> {
            try {
                webSocket.getSession().sendMessage(new BinaryMessage(message.getBody()));
            } catch (IOException e) {
                log.error("NOT FOUND WS WITH: {}", webSocket);
            }
        });


//        log.info("onMessage: {}", channel);
//        Set<String> userIds = this.webSocketSessionManager.getUserIdsListenerServer(channel);
//        log.info("UserIdsListenerServer: {}", userIds);
//        userIds.forEach(userId -> {
//            synchronized (userId) {
//                Set<WebSocketSession> wss = this.webSocketSessionManager.getWebSocketSession(userId);
////                log.info("check ws: {}", ws.getId());
//                wss.forEach(ws -> {
//                    try {
////                    ws.sendMessage(new TextMessage(body));
//                        ws.sendMessage(new BinaryMessage(message.getBody()));
//                    } catch (IOException e) {
//                        throw new RuntimeException(e);
//                    }
//                });
//            }
//        });
    }
}
