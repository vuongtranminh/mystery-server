package com.vuong.app.kafka;

import com.google.protobuf.Message;
import com.vuong.app.event.CreateUserEvent;
import com.vuong.app.event.UpdateUserEvent;
import com.vuong.app.event.UserEvent;
import io.confluent.kafka.serializers.protobuf.KafkaProtobufDeserializer;
import io.confluent.kafka.serializers.protobuf.KafkaProtobufDeserializerConfig;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.MessageListenerContainer;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaConsumerConfig {

//    public static final String CREATE_USER_KAFKA_LISTENER_CONTAINER_FACTORY = "createUserKafkaListenerContainerFactory";
//    public static final String UPDATE_USER_KAFKA_LISTENER_CONTAINER_FACTORY = "updateUserKafkaListenerContainerFactory";

    public static final String USER_KAFKA_LISTENER_CONTAINER_FACTORY = "userKafkaListenerContainerFactory";
    public static final String PROTOBUF_KAFKA_LISTENER_CONTAINER_FACTORY = "protobufKafkaListenerContainerFactory";

    // 1. Consume string data from Kafka

    @Bean
    public ConsumerFactory<String, String> consumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "group-id");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        return new DefaultKafkaConsumerFactory<>(props);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, String> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        return factory;
    }

    // 2. Consume user objects from Kafka

//    private Map<String, Object> consumerProps() {
//        Map<String, Object> props = new HashMap<>();
//        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
//        props.put(ConsumerConfig.GROUP_ID_CONFIG, "group-id");
//        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
//        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
//        props.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
//        return props;
//    }
//
//    @Bean
//    public ConsumerFactory<String, UserEvent> userConsumerFactory() {
//        return new DefaultKafkaConsumerFactory<>(consumerProps());
//    }
//
//    @Bean(USER_KAFKA_LISTENER_CONTAINER_FACTORY)
//    public ConcurrentKafkaListenerContainerFactory<String, UserEvent> createUserKafkaListenerContainerFactory(ConsumerFactory<String, UserEvent> consumerFactory) {
//        ConcurrentKafkaListenerContainerFactory<String, UserEvent> factory = new ConcurrentKafkaListenerContainerFactory<>();
//        factory.setConsumerFactory(consumerFactory);
//        return factory;
//    }

    // 3. Consume protobuf objects from Kafka

    private Map<String, Object> protobufConsumerProps() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "group-id");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, KafkaProtobufDeserializer.class);
        props.put("schema.registry.url", "http://127.0.0.1:8081");
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        return props;
    }

    @Bean
    public ConsumerFactory<String, Message> protobufConsumerFactory() {
        return new DefaultKafkaConsumerFactory<>(protobufConsumerProps());
    }

    @Bean(PROTOBUF_KAFKA_LISTENER_CONTAINER_FACTORY)
    public ConcurrentKafkaListenerContainerFactory<String, Message> protobufKafkaListenerContainerFactory(ConsumerFactory<String, Message> protobufConsumerFactory) {
        ConcurrentKafkaListenerContainerFactory<String, Message> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(protobufConsumerFactory);
        return factory;
    }

//    public ConsumerFactory<String, CreateUserEvent> createUserConsumerFactory() {
//        Map<String, Object> props = new HashMap<>();
//        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
//        props.put(ConsumerConfig.GROUP_ID_CONFIG, "group-id");
//        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
//        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
//        props.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
//        return new DefaultKafkaConsumerFactory<>(props);
//    }
//
//    @Bean(CREATE_USER_KAFKA_LISTENER_CONTAINER_FACTORY)
//    public ConcurrentKafkaListenerContainerFactory<String, CreateUserEvent> createUserKafkaListenerContainerFactory() {
//        ConcurrentKafkaListenerContainerFactory<String, CreateUserEvent> factory = new ConcurrentKafkaListenerContainerFactory<>();
//        factory.setConsumerFactory(createUserConsumerFactory());
//        return factory;
//    }
//
//    public ConsumerFactory<String, UpdateUserEvent> updateUserConsumerFactory() {
//        Map<String, Object> props = new HashMap<>();
//        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
//        props.put(ConsumerConfig.GROUP_ID_CONFIG, "group-id-2");
//        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
//        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
//        props.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
//        return new DefaultKafkaConsumerFactory<>(props);
//    }
//
//    @Bean(UPDATE_USER_KAFKA_LISTENER_CONTAINER_FACTORY)
//    public ConcurrentKafkaListenerContainerFactory<String, UpdateUserEvent> updateUserKafkaListenerContainerFactory() {
//        ConcurrentKafkaListenerContainerFactory<String, UpdateUserEvent> factory = new ConcurrentKafkaListenerContainerFactory<>();
//        factory.setConsumerFactory(updateUserConsumerFactory());
//        return factory;
//    }

}
