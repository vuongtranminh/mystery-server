package com.vuong.app.redis;

import com.vuong.app.doman.MemberProfile;
import com.vuong.app.doman.Message;
import com.vuong.app.doman.MessageEvent;
import com.vuong.app.redis.serializer.ProtobufSerializer;
import com.vuong.app.v1.discord.GrpcMessageEvent;
import com.vuong.app.websocket.WebSocketSessionManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.io.Serializable;
import java.util.Base64;
import java.util.Map;

@Service
@Slf4j
public class DefaultMessageDelegate implements MessageDelegate {

    private final WebSocketSessionManager webSocketSessionManager;
    private final ProtobufSerializer<GrpcMessageEvent> serializer;

    public DefaultMessageDelegate() {
        this.webSocketSessionManager = WebSocketSessionManager.getInstance();
        this.serializer = new ProtobufSerializer<GrpcMessageEvent>(GrpcMessageEvent.class);
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
        GrpcMessageEvent grpcEvent = serializer.deserialize(Base64.getDecoder().decode(message.toString()));
        MessageEvent event = MessageEvent.builder()
                .type(grpcEvent.getType().getNumber() == 1 ? "add" : "update")
                .message(Message.builder()
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

        log.info("got the message on redis "+ channel + " and "+ event.toString());
        WebSocketSession ws = this.webSocketSessionManager.getWebSocketSessions(channel);
        try {
            ws.sendMessage(new TextMessage(message.toString()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
