package com.vuong.app.kafka;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

@Service
@Slf4j
public class Producer {

    private static final String TOPIC = "purchases";

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    public void sendMessage(String key, String value) {
        ListenableFuture<SendResult<String, String>> future = kafkaTemplate.send(TOPIC, key, value);
        future.addCallback(new ListenableFutureCallback<SendResult<String, String>>() {
            @Override
            public void onSuccess(SendResult<String, String> result) {
                log.info(String.format("Produced event to topic %s: key = %-10s value = %s", TOPIC, key, value));
            }
            @Override
            public void onFailure(Throwable ex) {
                ex.printStackTrace();
            }
        });
    }
}
