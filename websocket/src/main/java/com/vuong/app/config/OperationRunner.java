package com.vuong.app.config;

import com.vuong.app.grpc.ServerClientService;
import com.vuong.app.redis.RedisMessageSubscriber;
import com.vuong.app.v1.discord.GrpcGetServerIdsRequest;
import com.vuong.app.v1.discord.GrpcGetServerIdsResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Order(value = 1)
@Component
@RequiredArgsConstructor
public class OperationRunner implements CommandLineRunner {

    private final ServerClientService serverClientService;
    private final RedisMessageSubscriber subscriber;

    @Override
    public void run(String... args) throws Exception {
        System.out.println("RUN 1");
        System.out.println("Insert data to db when start app in here!!!");
        GrpcGetServerIdsResponse response = this.serverClientService.getServerIds(GrpcGetServerIdsRequest.newBuilder().build());
        Set<String> serversId = new HashSet<>();
        response.getResultList().stream().forEach(serverId -> {
            serversId.add(serverId);
        });
        this.subscriber.subscribe(serversId);
    }
}
