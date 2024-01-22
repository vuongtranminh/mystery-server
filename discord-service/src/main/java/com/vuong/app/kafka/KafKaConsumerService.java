//package com.vuong.app.kafka;
//
//import com.vuong.app.event.CreateUserEvent;
//import com.vuong.app.event.UpdateUserEvent;
//import com.vuong.app.event.UserEvent;
//import com.vuong.app.model.CreateProfileRequest;
//import com.vuong.app.model.UpdateProfileRequest;
//import com.vuong.app.service.ProfileService;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.kafka.annotation.KafkaListener;
//import org.springframework.kafka.support.KafkaHeaders;
//import org.springframework.messaging.handler.annotation.Header;
//import org.springframework.stereotype.Service;
//
//@Service
//@Slf4j
//@RequiredArgsConstructor
//public class KafKaConsumerService {
//
//    private final ProfileService profileService;
//
//    private static final String CREATE_USER_KEY = "create-user-key";
//    private static final String UPDATE_USER_KEY = "update-user-key";
//
////    @KafkaListener(topics = "user-topic", groupId = "group-id", containerFactory = KafkaConsumerConfig.CREATE_USER_KAFKA_LISTENER_CONTAINER_FACTORY)
////    public void listen(CreateUserEvent value,
////                       @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
////                       @Header(KafkaHeaders.RECEIVED_MESSAGE_KEY) String key) {
////        log.info("Consumed event from topic {}: key = {} value = {}", topic, key, value);
////        this.profileService.createProfile(CreateProfileRequest.builder()
////                .profileId(value.getUserId())
////                .name(value.getName())
////                .avtUrl(value.getAvtUrl())
////                .build());
////    }
////
////    @KafkaListener(topics = "user-topic", groupId = "group-id", containerFactory = KafkaConsumerConfig.UPDATE_USER_KAFKA_LISTENER_CONTAINER_FACTORY)
////    public void listen(UpdateUserEvent value,
////                       @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
////                       @Header(KafkaHeaders.RECEIVED_MESSAGE_KEY) String key) {
////        log.info("Consumed event from topic {}: key = {} value = {}", topic, key, value);
////        this.profileService.updateProfile(UpdateProfileRequest.builder()
////                .profileId(value.getUserId())
////                .name(value.getName())
////                .avtUrl(value.getAvtUrl())
////                .build());
////    }
//
//    @KafkaListener(topics = "user-topic", groupId = "group-id", containerFactory = KafkaConsumerConfig.USER_KAFKA_LISTENER_CONTAINER_FACTORY)
//    public void listen(UserEvent value,
//                       @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
//                       @Header(KafkaHeaders.RECEIVED_MESSAGE_KEY) String key) {
//        log.info("Consumed event from topic {}: key = {} value = {}", topic, key, value);
//
//        if (CREATE_USER_KEY.equals(key)) {
//            this.createProfile(value);
//            return;
//        }
//
//        if (UPDATE_USER_KEY.equals(key)) {
//            this.updateProfile(value);
//            return;
//        }
//    }
//
//    private void createProfile(UserEvent value) {
//        this.profileService.createProfile(CreateProfileRequest.builder()
//                .profileId(value.getUserId())
//                .name(value.getName())
//                .avtUrl(value.getAvtUrl())
//                .build());
//    }
//
//    private void updateProfile(UserEvent value) {
//        this.profileService.createProfile(CreateProfileRequest.builder()
//                .profileId(value.getUserId())
//                .name(value.getName())
//                .avtUrl(value.getAvtUrl())
//                .build());
//    }
//}
