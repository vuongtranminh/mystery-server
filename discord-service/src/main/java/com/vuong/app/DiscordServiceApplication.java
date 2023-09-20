package com.vuong.app;

import com.vuong.app.jpa.specification.JpaSpecificationExecutorWithProjectionImpl;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories(repositoryBaseClass = JpaSpecificationExecutorWithProjectionImpl.class)
public class DiscordServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(DiscordServiceApplication.class, args);
	}

}
