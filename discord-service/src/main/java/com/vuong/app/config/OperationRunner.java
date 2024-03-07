package com.vuong.app.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Order(value = 2)
@Component
public class OperationRunner implements CommandLineRunner {
    @Override
    public void run(String... args) throws Exception {
        System.out.println("RUN 2");
        System.out.println("Insert data to db when start app in here!!!");
    }
}
