package com.vuong.app.kafka;

import com.google.protobuf.Message;
import com.vuong.app.v1.discord.GrpcCreateMessageRequest;
import io.confluent.kafka.serializers.protobuf.KafkaProtobufSerializer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaProducerConfig {

//    public static final String CREATE_USER_KAFKA_TEMPLATE_BEAN = "createUserKafkaTemplate";
//    public static final String UPDATE_USER_KAFKA_TEMPLATE_BEAN = "updateUserKafkaTemplate";

    public static final String USER_KAFKA_TEMPLATE_BEAN = "userKafkaTemplateBean";
    public static final String PROTOBUF_KAFKA_TEMPLATE_BEAN = "protobufKafkaTemplateBean";

    //1. Send string to Kafka
    public ProducerFactory<String, String> producerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        return new DefaultKafkaProducerFactory<>(props);
    }

    @Bean
    public KafkaTemplate<String, String> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }

    // 3. Send Event objects to Kafka
    @Bean
    public ProducerFactory<String, GrpcCreateMessageRequest> protobufProducerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, KafkaProtobufSerializer.class.getName());
        configProps.put("schema.registry.url", "http://127.0.0.1:8081");
        return new DefaultKafkaProducerFactory<>(configProps);
    }

    @Bean(PROTOBUF_KAFKA_TEMPLATE_BEAN)
    public KafkaTemplate<String, GrpcCreateMessageRequest> protobufKafkaTemplate(ProducerFactory<String, GrpcCreateMessageRequest> protobufProducerFactory) {
        return new KafkaTemplate<>(protobufProducerFactory);
    }

//    public ProducerFactory<String, CreateUserEvent> createUserProducerFactory() {
//        Map<String, Object> configProps = new HashMap<>();
//        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
//        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
//        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
//        return new DefaultKafkaProducerFactory<>(configProps);
//    }
//
//    @Bean(CREATE_USER_KAFKA_TEMPLATE_BEAN)
//    public KafkaTemplate<String, CreateUserEvent> createUserKafkaTemplate() {
//        return new KafkaTemplate<>(createUserProducerFactory());
//    }
//
//    public ProducerFactory<String, UpdateUserEvent> updateUserProducerFactory() {
//        Map<String, Object> configProps = new HashMap<>();
//        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
//        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
//        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
//        return new DefaultKafkaProducerFactory<>(configProps);
//    }
//
//    @Bean(UPDATE_USER_KAFKA_TEMPLATE_BEAN)
//    public KafkaTemplate<String, UpdateUserEvent> updateUserKafkaTemplate() {
//        return new KafkaTemplate<>(updateUserProducerFactory());
//    }
}
