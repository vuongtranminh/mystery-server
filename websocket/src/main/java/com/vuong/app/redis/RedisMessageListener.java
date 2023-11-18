package com.vuong.app.redis;

import com.vuong.app.doman.MemberProfile;
import com.vuong.app.doman.MessageEvent;
import com.vuong.app.redis.serializer.ProtobufSerializer;
import com.vuong.app.v1.discord.GrpcMessageEvent;
import com.vuong.app.websocket.WebSocketSessionManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.Base64;
import java.util.Set;

@Service
@Slf4j
public class RedisMessageListener implements MessageListener {

    private final WebSocketSessionManager webSocketSessionManager;
    private final ProtobufSerializer<GrpcMessageEvent> serializer;

    public RedisMessageListener() {
        this.webSocketSessionManager = WebSocketSessionManager.getInstance();
        this.serializer = new ProtobufSerializer<GrpcMessageEvent>(GrpcMessageEvent.class);
    }

    @Override
    public void onMessage(Message message, byte[] pattern) {
        String channel = new String(message.getChannel());
//        String body = new String(message.getBody());

        GrpcMessageEvent grpcEvent = serializer.deserialize(message.getBody());
        MessageEvent event = MessageEvent.builder()
                .type(grpcEvent.getType().getNumber() == 1 ? "add" : "update")
                .message(com.vuong.app.doman.Message.builder()
                        .messageId(grpcEvent.getMessage().getMessageId())
                        .content(grpcEvent.getMessage().getContent())
                        .fileUrl(grpcEvent.getMessage().getFileUrl())
                        .channelId(grpcEvent.getMessage().getChannelId())
                        .createdAt(grpcEvent.getMessage().getCreatedAt())
                        .updatedAt(grpcEvent.getMessage().getUpdatedAt())
                        .deletedAt(grpcEvent.getMessage().getDeletedAt())
                        .deletedBy(grpcEvent.getMessage().getDeletedBy())
                        .author(MemberProfile.builder()
                                .memberId(grpcEvent.getMessage().getAuthor().getMemberId())
                                .role(grpcEvent.getMessage().getAuthor().getRole().getNumber())
                                .serverId(grpcEvent.getMessage().getAuthor().getServerId())
                                .joinAt(grpcEvent.getMessage().getAuthor().getJoinAt())
                                .profileId(grpcEvent.getMessage().getAuthor().getProfileId())
                                .name(grpcEvent.getMessage().getAuthor().getName())
                                .avtUrl(grpcEvent.getMessage().getAuthor().getAvtUrl())
                                .build())
                        .build())
                .build();

        String body = event.toString();

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
