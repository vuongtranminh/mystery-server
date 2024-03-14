package com.vuong.app.websocket;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

import javax.annotation.PostConstruct;

@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketConfigurer {

    private final WebSocketHandler webSocketHandler;
    private final WebSocketAuthInterceptor webSocketAuthInterceptor;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(this.webSocketHandler, "/mystery/*")
                .addInterceptors(webSocketAuthInterceptor)
                .setAllowedOrigins("*");
        registry.addHandler(this.webSocketHandler, "/mystery")
                .addInterceptors(webSocketAuthInterceptor)
                .setAllowedOrigins("*");
    }

    @PostConstruct
    public void init() {
        // Thực hiện các công việc khởi tạo sau khi bean được tạo ra
        System.out.println("Bean initialization logic");
    }

//    @Bean
//    public WebSocketHandler myHandler() {
//        return new SocketTextHandler();
//    }
}
