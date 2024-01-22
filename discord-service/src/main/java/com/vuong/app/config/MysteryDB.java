package com.vuong.app.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
public class MysteryDB {
    @Bean
    public DataSource dataSource() {
        HikariConfig config = new HikariConfig("discord-service/src/main/resources/hikari.properties");
        DataSource ds = new HikariDataSource(config);
        return ds;
    }
}
