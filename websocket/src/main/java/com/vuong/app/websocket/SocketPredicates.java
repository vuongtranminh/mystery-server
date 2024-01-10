package com.vuong.app.websocket;

import org.springframework.web.socket.WebSocketSession;

import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class SocketPredicates {
    public static Predicate<WebSocket> findBySession(WebSocketSession session) {
        return s -> s.getSession().getId().equals(session.getId());
    }

    public static Predicate<WebSocket> findByServerId(String serverId) {
        return s -> s.getServerId().equals(serverId);
    }

    public static Set<WebSocket> filterWebsockets(Set<WebSocket> webSockets, Predicate<WebSocket> predicate) {
        return webSockets.stream().filter(predicate).collect(Collectors.toUnmodifiableSet());
    }
}
