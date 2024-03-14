package com.vuong.app.websocket;

import com.vuong.app.config.AppProperties;
import com.vuong.app.security.TokenProvider;
import com.vuong.app.util.CookieUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class WebSocketAuthInterceptor implements HandshakeInterceptor {

    private final AppProperties appProperties;
    private final TokenProvider tokenProvider;

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
//        String path = request.getURI().getPath();
//        String userId = WebSocketHelper.getUserIdFromUrl(path);
//        attributes.put(WebSocketHelper.userIdKey, userId);
//        return true;

        if (!(request instanceof ServletServerHttpRequest)) {
            return false;
        }

        ServletServerHttpRequest servletServerRequest = (ServletServerHttpRequest) request;
        HttpServletRequest servletRequest = servletServerRequest.getServletRequest();

        String jwt = CookieUtils.getCookie(servletRequest, appProperties.getAuth().getAccessTokenCookieName())
                .map(cookie -> CookieUtils.deserialize(cookie, String.class))
                .orElse(null);

        if (!StringUtils.hasText(jwt) || !tokenProvider.validateAccessToken(jwt)) {
            return false;
        }

        String userId = tokenProvider.extractUserIdFromAccessToken(jwt);
        attributes.put(WebSocketHelper.USER_ID_KEY, userId);

        return true;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Exception exception) {

    }
}
