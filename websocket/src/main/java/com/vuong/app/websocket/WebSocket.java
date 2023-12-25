package com.vuong.app.websocket;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.socket.WebSocketSession;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class WebSocket {
    private WebSocketSession session;
    private String userId;
    private String serverId;
}
