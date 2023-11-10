package com.vuong.app.kafka;

import com.vuong.app.event.CreateUserEvent;
import com.vuong.app.event.UpdateUserEvent;
import com.vuong.app.event.UserEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
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

//    @Qualifier(KafkaProducerConfig.CREATE_USER_KAFKA_TEMPLATE_BEAN)
//    private final KafkaTemplate<String, CreateUserEvent> createUserKafkaTemplate;
//
//    @Qualifier(KafkaProducerConfig.UPDATE_USER_KAFKA_TEMPLATE_BEAN)
//    private final KafkaTemplate<String, UpdateUserEvent> updateUserKafkaTemplate;

    @Qualifier(KafkaProducerConfig.USER_KAFKA_TEMPLATE_BEAN)
    private final KafkaTemplate<String, UserEvent> userKafkaTemplate;

//    public KafKaProducerService(@Qualifier(KafkaProducerConfig.USER_KAFKA_TEMPLATE_BEAN) KafkaTemplate<String, UserEvent> userKafkaTemplate) {
//        this.userKafkaTemplate = userKafkaTemplate;
//    }

    public void sendMessage(String key, UserEvent data) {
        ListenableFuture<SendResult<String, UserEvent>> future = userKafkaTemplate.send(TOPIC, key, data);
        future.addCallback(new KafkaSendCallback<String, UserEvent>() {

            @Override
            public void onSuccess(SendResult<String, UserEvent> result) {
                log.info("Sent message: " + data
                        + " with offset: " + result.getRecordMetadata().offset());
            }

            @Override
            public void onFailure(KafkaProducerException ex) {
                ProducerRecord<String, UserEvent> failed = ex.getFailedProducerRecord();
                log.error("Unable to send message : " + data, ex);
            }

        });
    }

//    public void sendMessage(String key, UpdateUserEvent data) {
//        ListenableFuture<SendResult<String, UpdateUserEvent>> future = updateUserKafkaTemplate.send(TOPIC, key, data);
//        future.addCallback(new KafkaSendCallback<String, UpdateUserEvent>() {
//
//            @Override
//            public void onSuccess(SendResult<String, UpdateUserEvent> result) {
//                log.info("Sent message: " + data
//                        + " with offset: " + result.getRecordMetadata().offset());
//            }
//
//            @Override
//            public void onFailure(KafkaProducerException ex) {
//                ProducerRecord<String, UpdateUserEvent> failed = ex.getFailedProducerRecord();
//                log.error("Unable to send message : " + data, ex);
//            }
//
//        });
//    }
}
