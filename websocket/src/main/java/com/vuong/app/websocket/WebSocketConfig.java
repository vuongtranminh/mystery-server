package com.vuong.app.websocket;

import com.vuong.app.grpc.ServerClientService;
import com.vuong.app.redis.RedisMessageSubscriber;
import com.vuong.app.v1.discord.GrpcGetServerIdsRequest;
import com.vuong.app.v1.discord.GrpcGetServerIdsResponse;
import com.vuong.app.v1.discord.GrpcGetServerJoinIdsRequest;
import com.vuong.app.v1.discord.GrpcGetServerJoinIdsResponse;
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

import javax.annotation.PostConstruct;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketConfigurer {

    private final SocketTextHandler socketTextHandler;
    private final WebSocketAuthInterceptor webSocketAuthInterceptor;
    private final ServerClientService serverClientService;
    private final RedisMessageSubscriber subscriber;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(this.socketTextHandler, "/mystery/*")
                .addInterceptors(webSocketAuthInterceptor)
                .setAllowedOrigins("*");
        registry.addHandler(this.socketTextHandler, "/mystery")
                .addInterceptors(webSocketAuthInterceptor)
                .setAllowedOrigins("*");
    }

    @PostConstruct
    public void init() {
        // Thực hiện các công việc khởi tạo sau khi bean được tạo ra
        System.out.println("Bean initialization logic");
        GrpcGetServerIdsResponse response = this.serverClientService.getServerIds(GrpcGetServerIdsRequest.newBuilder().build());
        Set<String> serversId = new HashSet<>();
        response.getResultList().stream().forEach(serverId -> {
            serversId.add(serverId);
        });
        this.subscriber.subscribe(serversId);
    }

//    @Bean
//    public WebSocketHandler myHandler() {
//        return new SocketTextHandler();
//    }
}
