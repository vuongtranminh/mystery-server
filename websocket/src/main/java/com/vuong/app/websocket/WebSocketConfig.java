package com.vuong.app.websocket;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketConfigurer {

    private final SocketTextHandler socketTextHandler;
    private final WebSocketAuthInterceptor webSocketAuthInterceptor;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(this.socketTextHandler, "/mystery/*")
                .addInterceptors(webSocketAuthInterceptor)
                .setAllowedOrigins("*");
        registry.addHandler(this.socketTextHandler, "/mystery")
                .addInterceptors(webSocketAuthInterceptor)
                .setAllowedOrigins("*");
    }

//    @Bean
//    public WebSocketHandler myHandler() {
//        return new SocketTextHandler();
//    }
}
