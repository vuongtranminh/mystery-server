package com.vuong.app.kafka;

import com.vuong.app.event.CreateUserEvent;
import com.vuong.app.event.UpdateUserEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class KafKaConsumerService {

    @KafkaListener(topics = "user-topic", groupId = "group-id", containerFactory = "createUserKafkaListenerContainerFactory")
    public void listen(CreateUserEvent value,
                       @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                       @Header(KafkaHeaders.RECEIVED_MESSAGE_KEY) String key) {
        log.info("Consumed event from topic {}: key = {} value = {}", topic, key, value);
    }

    @KafkaListener(topics = "user-topic", groupId = "group-id", containerFactory = "updateUserKafkaListenerContainerFactory")
    public void listen(UpdateUserEvent value,
                       @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                       @Header(KafkaHeaders.RECEIVED_MESSAGE_KEY) String key) {
        log.info("Consumed event from topic {}: key = {} value = {}", topic, key, value);
    }
}
