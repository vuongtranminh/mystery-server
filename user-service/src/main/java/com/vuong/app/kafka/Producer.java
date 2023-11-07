package com.vuong.app.kafka;

import com.vuong.app.event.CreateUserEvent;
import com.vuong.app.event.UpdateUserEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

@Service
@Slf4j
@RequiredArgsConstructor
public class Producer {

    private static final String TOPIC = "purchases";

    private final KafkaTemplate<String, CreateUserEvent> createUserKafkaTemplate;
    private final KafkaTemplate<String, UpdateUserEvent> updateUserKafkaTemplate;

    public void sendMessage(String key, CreateUserEvent value) {
        ListenableFuture<SendResult<String, CreateUserEvent>> future = createUserKafkaTemplate.send(TOPIC, key, value);
        future.addCallback(new ListenableFutureCallback<SendResult<String, CreateUserEvent>>() {
            @Override
            public void onSuccess(SendResult<String, CreateUserEvent> result) {
                log.info("Produced event to topic {}: key = {} value = {}", TOPIC, key, value);
            }
            @Override
            public void onFailure(Throwable ex) {
                ex.printStackTrace();
            }
        });
    }

    public void sendMessage(String key, UpdateUserEvent value) {
        ListenableFuture<SendResult<String, UpdateUserEvent>> future = updateUserKafkaTemplate.send(TOPIC, key, value);
        future.addCallback(new ListenableFutureCallback<SendResult<String, UpdateUserEvent>>() {
            @Override
            public void onSuccess(SendResult<String, UpdateUserEvent> result) {
                log.info("Produced event to topic {}: key = {} value = {}", TOPIC, key, value);
            }
            @Override
            public void onFailure(Throwable ex) {
                ex.printStackTrace();
            }
        });
    }
}
