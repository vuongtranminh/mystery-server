package com.vuong.app.kafka;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@Slf4j
public class Consumer {

    @KafkaListener(id = "myConsumer", topics = "purchases", groupId = "spring-boot", autoStartup = "false")
    public void listen(String value,
                       @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                       @Header(KafkaHeaders.RECEIVED_MESSAGE_KEY) String key) {
        log.info(String.format("Consumed event from topic %s: key = %-10s value = %s", topic, key, value));
    }
}
