package com.vuong.app.websocket;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.socket.WebSocketSession;

import java.util.*;

@Slf4j
public class WebSocketSessionManager {
    //    Double Check Locking Singleton
    private static volatile WebSocketSessionManager instance;

    private final Map<String, WebSocketSession> webSocketSessionByUserId;
    private final Map<String, Set<WebSocketSession>> webSocketSessionByChannel;
    private final Map<String, List<String>> managerServerIdsByUserId;

    private WebSocketSessionManager() {
        this.webSocketSessionByUserId = new HashMap<>();
        this.webSocketSessionByChannel = new HashMap<>();
        this.managerServerIdsByUserId = new HashMap<>();
    }

    public static WebSocketSessionManager getInstance() {
        if (instance == null) {
            // Do the task too long before create instance ...
            // Block so other threads cannot come into while initialize
            synchronized (WebSocketSessionManager.class) {
                // Re-check again. Maybe another thread has initialized before
                if (instance == null) {
                    instance = new WebSocketSessionManager();
                }
            }
        }
        // Do something after get instance ...
        return instance;
    }

    public void addWebSocketSession(WebSocketSession webSocketSession){
        String userId = WebSocketHelper.getUserIdFromSessionAttribute(webSocketSession);
        log.info("got request to add session id {} for user id {} ", webSocketSession.getId(), userId);
        this.webSocketSessionByUserId.put(userId,webSocketSession);
        log.info("added session id {} for user id {}", webSocketSession.getId(), userId);
    }

    public void removeWebSocketSession(WebSocketSession webSocketSession){
        String userId = WebSocketHelper.getUserIdFromSessionAttribute(webSocketSession);
        log.info("got request to remove session id {} for user id {}", webSocketSession.getId(), userId);
        this.webSocketSessionByUserId.remove(userId);
        log.info("removed session id {} for user id {}", webSocketSession.getId(), userId);
    }

    public void addWebSocketSessionToChannel(String channel, WebSocketSession webSocketSession) {
        if (this.webSocketSessionByChannel.containsKey(channel)) {
            Set<WebSocketSession> webSocketSessions = this.webSocketSessionByChannel.get(channel);
            webSocketSessions.add(webSocketSession);
        } else {
            Set<WebSocketSession> webSocketSessions = new HashSet<>();
            webSocketSessions.add(webSocketSession);
            this.webSocketSessionByChannel.put(channel, webSocketSessions);
        }
    }

    public void removeWebSocketSessionToChannel(String channel, WebSocketSession webSocketSession) {
        if (!this.webSocketSessionByChannel.containsKey(channel)) {
            return;
        }

        Set<WebSocketSession> webSocketSessions = this.webSocketSessionByChannel.get(channel);
        webSocketSessions.remove(webSocketSession);
    }

    public Set<WebSocketSession> getWebSocketSessionsByChannel(String channel) {
        return this.webSocketSessionByChannel.get(channel);
    }

    public WebSocketSession getWebSocketSessions(String userId) {
        return this.webSocketSessionByUserId.get(userId);
    }


    public void addServerIdsByUserId(String userId, List<String> serverIds) {
        this.managerServerIdsByUserId.put(userId, serverIds);
    }

    public void removeServerIdsByUserId(String userId) {
        this.managerServerIdsByUserId.remove(userId);
    }

    public List<String> getServerIdsByUserId(String userId) {
        return this.managerServerIdsByUserId.get(userId);
    }
}
