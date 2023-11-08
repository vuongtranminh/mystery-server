package com.vuong.app.kafka;

import com.vuong.app.event.CreateUserEvent;
import com.vuong.app.event.UpdateUserEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaFailureCallback;
import org.springframework.kafka.core.KafkaProducerException;
import org.springframework.kafka.core.KafkaSendCallback;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

@Service
@Slf4j
@RequiredArgsConstructor
public class KafKaProducerService {

    private static final String TOPIC = "user-topic";

    private final KafkaTemplate<String, CreateUserEvent> createUserKafkaTemplate;
    private final KafkaTemplate<String, UpdateUserEvent> updateUserKafkaTemplate;

    public void sendMessage(String key, CreateUserEvent data) {
        ListenableFuture<SendResult<String, CreateUserEvent>> future = createUserKafkaTemplate.send(TOPIC, key, data);
        future.addCallback(new KafkaSendCallback<String, CreateUserEvent>() {

            @Override
            public void onSuccess(SendResult<String, CreateUserEvent> result) {
                log.info("Sent message: " + data
                        + " with offset: " + result.getRecordMetadata().offset());
            }

            @Override
            public void onFailure(KafkaProducerException ex) {
                ProducerRecord<String, CreateUserEvent> failed = ex.getFailedProducerRecord();
                log.error("Unable to send message : " + data, ex);
            }

        });
    }

    public void sendMessage(String key, UpdateUserEvent data) {
        ListenableFuture<SendResult<String, UpdateUserEvent>> future = updateUserKafkaTemplate.send(TOPIC, key, data);
        future.addCallback(new KafkaSendCallback<String, UpdateUserEvent>() {

            @Override
            public void onSuccess(SendResult<String, UpdateUserEvent> result) {
                log.info("Sent message: " + data
                        + " with offset: " + result.getRecordMetadata().offset());
            }

            @Override
            public void onFailure(KafkaProducerException ex) {
                ProducerRecord<String, UpdateUserEvent> failed = ex.getFailedProducerRecord();
                log.error("Unable to send message : " + data, ex);
            }

        });
    }
}
