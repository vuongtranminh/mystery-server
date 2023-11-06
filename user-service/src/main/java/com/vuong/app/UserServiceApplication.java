package com.vuong.app;

import com.vuong.app.kafka.Producer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.listener.MessageListenerContainer;

@SpringBootApplication
public class UserServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(UserServiceApplication.class, args);
	}

	private final Producer producer;

	@Bean
	public CommandLineRunner CommandLineRunnerBean() {
		return (args) -> {
			for (String arg : args) {
				switch (arg) {
					case "--producer":
						this.producer.sendMessage("awalther", "t-shirts");
						this.producer.sendMessage("htanaka", "t-shirts");
						this.producer.sendMessage("htanaka", "batteries");
						this.producer.sendMessage("eabara", "t-shirts");
						this.producer.sendMessage("htanaka", "t-shirts");
						this.producer.sendMessage("jsmith", "book");
						this.producer.sendMessage("awalther", "t-shirts");
						this.producer.sendMessage("jsmith", "batteries");
						this.producer.sendMessage("jsmith", "gift card");
						this.producer.sendMessage("eabara", "t-shirts");
						break;
					case "--consumer":
						MessageListenerContainer listenerContainer = kafkaListenerEndpointRegistry.getListenerContainer("myConsumer");
						listenerContainer.start();
						break;
					default:
						break;
				}
			}
		};
	}

	@Autowired
	UserServiceApplication(Producer producer) {
		this.producer = producer;
	}

	@Autowired
	private KafkaListenerEndpointRegistry kafkaListenerEndpointRegistry;

}
