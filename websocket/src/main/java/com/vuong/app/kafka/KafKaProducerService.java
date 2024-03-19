package com.vuong.app.kafka;

import com.google.protobuf.Message;
import com.vuong.app.v1.discord.GrpcCreateMessageRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.core.KafkaProducerException;
import org.springframework.kafka.core.KafkaSendCallback;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;

@Service
@Slf4j
@RequiredArgsConstructor
public class KafKaProducerService {

    private static final String TOPIC = "user-topic";

    private static final String PROTOBUF_TOPIC = "protobuf-topic";

//    @Qualifier(KafkaProducerConfig.CREATE_USER_KAFKA_TEMPLATE_BEAN)
//    private final KafkaTemplate<String, CreateUserEvent> createUserKafkaTemplate;
//
//    @Qualifier(KafkaProducerConfig.UPDATE_USER_KAFKA_TEMPLATE_BEAN)
//    private final KafkaTemplate<String, UpdateUserEvent> updateUserKafkaTemplate;

    @Qualifier(KafkaProducerConfig.PROTOBUF_KAFKA_TEMPLATE_BEAN)
    private final KafkaTemplate<String, GrpcCreateMessageRequest> protobufKafkaTemplate;

//    public KafKaProducerService(@Qualifier(KafkaProducerConfig.USER_KAFKA_TEMPLATE_BEAN) KafkaTemplate<String, UserEvent> userKafkaTemplate) {
//        this.userKafkaTemplate = userKafkaTemplate;
//    }

    public void sendMessage(String key, GrpcCreateMessageRequest data) {
        log.info("Kafka pub: {} : {}", PROTOBUF_TOPIC, key);
        ListenableFuture<SendResult<String, GrpcCreateMessageRequest>> future = protobufKafkaTemplate.send(PROTOBUF_TOPIC, key, data);
        future.addCallback(new KafkaSendCallback<String, GrpcCreateMessageRequest>() {

            @Override
            public void onSuccess(SendResult<String, GrpcCreateMessageRequest> result) {
                log.info("Sent message: " + data
                        + " with offset: " + result.getRecordMetadata().offset());
            }

            @Override
            public void onFailure(KafkaProducerException ex) {
                ProducerRecord<String, GrpcCreateMessageRequest> failed = ex.getFailedProducerRecord();
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
