package com.vuong.app.websocket;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;


// update to store redis
@Component
@Slf4j
public class WebSocketSessionManager {
    //    Double Check Locking Singleton
    private final ConcurrentMap<String, Set<String>> managerWebSocketSessionIdsByServerId;
    private final ConcurrentMap<String, WebSocketSession> managerWebSocketSessionByWebSocketSessionId;

    public WebSocketSessionManager() {
        this.managerWebSocketSessionIdsByServerId = new ConcurrentHashMap<>();
        this.managerWebSocketSessionByWebSocketSessionId = new ConcurrentHashMap<>();
    }

    public WebSocketSession getWebSocketSessionByWebSocketSessionId(String webSocketSessionId) {
        return this.managerWebSocketSessionByWebSocketSessionId.getOrDefault(webSocketSessionId, null);
    }

    public void addWebSocketSessionToManagerWebSocketSessionByWebSocketSessionId(WebSocketSession webSocketSession) {
        this.managerWebSocketSessionByWebSocketSessionId.put(webSocketSession.getId(), webSocketSession);
    }

    public void removeWebSocketSessionFromManagerWebSocketSessionByWebSocketSessionId(WebSocketSession webSocketSession) {
        this.managerWebSocketSessionByWebSocketSessionId.remove(webSocketSession.getId());
    }

    public Set<String> getWebSocketSessionIdsByServerId(String serverId) {
        return this.managerWebSocketSessionIdsByServerId.getOrDefault(serverId, new HashSet<>());
    }

    public void addWebSocketSessionIdToManagerWebSocketSessionIdsByServerId(String serverId, String webSocketSessionId) {
        Set<String> webSocketSessionIds = this.getWebSocketSessionIdsByServerId(serverId);
        webSocketSessionIds.add(webSocketSessionId);
    }

    public void removeWebSocketSessionIdFromManagerWebSocketSessionIdsByServerId(String serverId, String webSocketSessionId) {
        Set<String> webSocketSessionIds = this.getWebSocketSessionIdsByServerId(serverId);
        webSocketSessionIds.remove(webSocketSessionId);
    }
    ////////////////////
//    private final Set<WebSocket> managerWebSocket;
//
//    public WebSocketSessionManager() {
//        this.managerWebSocket = new HashSet<>();
//    }
//
//    public void addWs(WebSocket ws) {
//        this.managerWebSocket.add(ws);
//    }
//
//    public void removeWsBySession(WebSocketSession session) {
//        this.managerWebSocket.removeIf(webSocket -> webSocket.getSession().getId().equals(session.getId()));
//    }
//
//    public Set<WebSocket> getWebSocketsByServerId(String serverId) {
//        return SocketPredicates.filterWebsockets(this.managerWebSocket, SocketPredicates.findByServerId(serverId));
////        return this.managerWebSocket.stream().filter(webSocket -> webSocket.getServerId().equals(serverId)).collect(Collectors.toUnmodifiableSet());
//    }
//
//    public Set<WebSocket> getWebSocketsBySession(WebSocketSession session) {
//        return SocketPredicates.filterWebsockets(this.managerWebSocket, SocketPredicates.findBySession(session));
////        return this.managerWebSocket.stream().filter(webSocket -> webSocket.getSession().getId().equals(session.getId())).collect(Collectors.toUnmodifiableSet());
//    }

}
