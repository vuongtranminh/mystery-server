package com.vuong.app.kafka;

import com.vuong.app.event.CreateUserEvent;
import com.vuong.app.event.UpdateUserEvent;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaProducerConfig {

//    public static final String CREATE_USER_KAFKA_TEMPLATE_BEAN = "createUserKafkaTemplate";
//    public static final String UPDATE_USER_KAFKA_TEMPLATE_BEAN = "updateUserKafkaTemplate";

    public static final String USER_KAFKA_TEMPLATE_BEAN = "userKafkaTemplate";

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

    //2. Send User objects to Kafka
    public ProducerFactory<String, CreateUserEvent> userProducerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        return new DefaultKafkaProducerFactory<>(configProps);
    }

    @Bean(USER_KAFKA_TEMPLATE_BEAN)
    public KafkaTemplate<String, CreateUserEvent> userKafkaTemplate() {
        return new KafkaTemplate<>(userProducerFactory());
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
