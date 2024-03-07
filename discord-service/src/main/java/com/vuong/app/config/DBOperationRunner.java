package com.vuong.app.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Order(value = 1)
@Component
public class DBOperationRunner implements CommandLineRunner {
    @Override
    public void run(String... args) throws Exception {
        System.out.println("RUN 1");
        System.out.println("Insert data to db when start app in here!!!");
    }
}
