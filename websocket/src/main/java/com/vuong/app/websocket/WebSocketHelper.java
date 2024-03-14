package com.vuong.app.websocket;

import org.springframework.web.socket.WebSocketSession;

import java.util.Set;

public class WebSocketHelper {
    public static final String USER_ID_KEY = "USER_ID";
    public static final String SERVER_IDS_KEY = "SERVER_IDS";

    public static String getUserIdFromSessionAttribute(WebSocketSession webSocketSession) {
        return (String) webSocketSession.getAttributes().get(USER_ID_KEY);
    }

    public static String getUserIdFromUrl(String path){
        return path.substring(path.lastIndexOf('/') + 1);
    }

    public static Set<String> getServerIdsFromSessionAttribute(WebSocketSession webSocketSession) {
        return (Set<String>) webSocketSession.getAttributes().get(SERVER_IDS_KEY);
    }

    public static void addServerIdsToSessionAttribute(WebSocketSession webSocketSession, Set<String> serverIds) {
        webSocketSession.getAttributes().put(SERVER_IDS_KEY, serverIds);
    }

    public static void removeServerIdFromSessionAttribute(WebSocketSession webSocketSession, String serverId) {
        Set<String> serverIds = (Set<String>) webSocketSession.getAttributes().get(SERVER_IDS_KEY);
        serverIds.remove(serverId);
    }
}
