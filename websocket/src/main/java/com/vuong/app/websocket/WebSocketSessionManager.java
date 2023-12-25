package com.vuong.app.websocket;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;


// update to store redis
@Component
@Slf4j
public class WebSocketSessionManager {
    //    Double Check Locking Singleton
    private final Set<WebSocket> managerWebSocket;

    public WebSocketSessionManager() {
        this.managerWebSocket = new HashSet<>();
    }

    public void addWs(WebSocket ws) {
        this.managerWebSocket.add(ws);
    }

    public void removeWsBySession(WebSocketSession session) {
        this.managerWebSocket.removeIf(webSocket -> webSocket.getSession().getId().equals(session.getId()));
    }

    public Set<WebSocket> getWebSocketsByServerId(String serverId) {
        return this.managerWebSocket.stream().filter(webSocket -> webSocket.getServerId().equals(serverId)).collect(Collectors.toUnmodifiableSet());
    }

    public Set<WebSocket> getWebSocketsBySession(WebSocketSession session) {
        return this.managerWebSocket.stream().filter(webSocket -> webSocket.getSession().getId().equals(session.getId())).collect(Collectors.toUnmodifiableSet());
    }






    // old

//    private final Map<String, WebSocketSession> managerWebSocketSessionByUserId;
//    private final Map<String, Set<String>> managerUserIdsByServerId;
//    private final Map<String, Set<String>> managerServerIdsByUserId;
//
//    public WebSocketSessionManager() {
//        this.managerWebSocketSessionByUserId = new ConcurrentHashMap<>();
//        this.managerUserIdsByServerId = new ConcurrentHashMap<>();
//        this.managerServerIdsByUserId = new ConcurrentHashMap<>();
//    }
//
//    public void addWebSocketSession(String userId, WebSocketSession webSocketSession) {
//        this.managerWebSocketSessionByUserId.put(userId, webSocketSession);
//        log.info("added session id {} for user id {}", webSocketSession.getId(), userId);
//    }
//
//    public WebSocketSession getWebSocketSession(String userId) {
//        return this.managerWebSocketSessionByUserId.get(userId);
//    }
//
//    public void removeWebSocketSession(String userId) {
//        this.managerWebSocketSessionByUserId.remove(userId);
//        log.info("removed session id for user id {}", userId);
//    }
//
//    public void addUserIdListenerServer(String serverId, String userId) {
//        Set<String> userIds = this.managerUserIdsByServerId.getOrDefault(serverId, new HashSet<>());
//        userIds.add(userId);
//        this.managerUserIdsByServerId.put(serverId, userIds);
//    }
//
//    public Set<String> getUserIdsListenerServer(String serverId) {
//        return this.managerUserIdsByServerId.get(serverId);
//    }
//
//    public void removeUserIdListenerServer(String serverId, String userId) {
//        Set<String> userIds = this.managerUserIdsByServerId.get(serverId);
//        userIds.remove(userId);
//        if (userIds.size() == 0) {
//            this.managerUserIdsByServerId.remove(serverId);
//        } else {
//            this.managerUserIdsByServerId.put(serverId, userIds);
//        }
//    }
//
//    public void addServerIds(String userId, Set<String> serverIds) {
//        this.managerServerIdsByUserId.put(userId, serverIds);
//    }
//
//    public Set<String> getServerIds(String userId) {
//        return this.managerServerIdsByUserId.get(userId);
//    }
//
//    public void removeServerIds(String userId) {
//        this.managerServerIdsByUserId.remove(userId);
//    }





    // old
//
//    private final Map<String, WebSocketSession> webSocketSessionByUserId;
//    private final Map<String, Set<WebSocketSession>> webSocketSessionByChannel;
//    private final Map<String, List<String>> managerServerIdsByUserId;
//
//    private WebSocketSessionManager() {
//        this.webSocketSessionByUserId = new HashMap<>();
//        this.webSocketSessionByChannel = new HashMap<>();
//        this.managerServerIdsByUserId = new HashMap<>();
//    }
//
//    public static WebSocketSessionManager getInstance() {
//        if (instance == null) {
//            // Do the task too long before create instance ...
//            // Block so other threads cannot come into while initialize
//            synchronized (WebSocketSessionManager.class) {
//                // Re-check again. Maybe another thread has initialized before
//                if (instance == null) {
//                    instance = new WebSocketSessionManager();
//                }
//            }
//        }
//        // Do something after get instance ...
//        return instance;
//    }
//
//    public void addWebSocketSession(WebSocketSession webSocketSession){
//        String userId = WebSocketHelper.getUserIdFromSessionAttribute(webSocketSession);
//        log.info("got request to add session id {} for user id {} ", webSocketSession.getId(), userId);
//        this.webSocketSessionByUserId.put(userId,webSocketSession);
//        log.info("added session id {} for user id {}", webSocketSession.getId(), userId);
//    }
//
//    public void removeWebSocketSession(WebSocketSession webSocketSession){
//        String userId = WebSocketHelper.getUserIdFromSessionAttribute(webSocketSession);
//        log.info("got request to remove session id {} for user id {}", webSocketSession.getId(), userId);
//        this.webSocketSessionByUserId.remove(userId);
//        log.info("removed session id {} for user id {}", webSocketSession.getId(), userId);
//    }
//
//    public void addWebSocketSessionToChannel(String channel, WebSocketSession webSocketSession) {
//        if (this.webSocketSessionByChannel.containsKey(channel)) {
//            Set<WebSocketSession> webSocketSessions = this.webSocketSessionByChannel.get(channel);
//            webSocketSessions.add(webSocketSession);
//        } else {
//            Set<WebSocketSession> webSocketSessions = new HashSet<>();
//            webSocketSessions.add(webSocketSession);
//            this.webSocketSessionByChannel.put(channel, webSocketSessions);
//        }
//    }
//
//    public void removeWebSocketSessionToChannel(String channel, WebSocketSession webSocketSession) {
//        if (!this.webSocketSessionByChannel.containsKey(channel)) {
//            return;
//        }
//
//        Set<WebSocketSession> webSocketSessions = this.webSocketSessionByChannel.get(channel);
//        webSocketSessions.remove(webSocketSession);
//    }
//
//    public Set<WebSocketSession> getWebSocketSessionsByChannel(String channel) {
//        return this.webSocketSessionByChannel.get(channel);
//    }
//
//    public WebSocketSession getWebSocketSessions(String userId) {
//        return this.webSocketSessionByUserId.get(userId);
//    }
//
//
//    public void addServerIdsByUserId(String userId, List<String> serverIds) {
//        this.managerServerIdsByUserId.put(userId, serverIds);
//    }
//
//    public void removeServerIdsByUserId(String userId) {
//        this.managerServerIdsByUserId.remove(userId);
//    }
//
//    public List<String> getServerIdsByUserId(String userId) {
//        return this.managerServerIdsByUserId.get(userId);
//    }
}
