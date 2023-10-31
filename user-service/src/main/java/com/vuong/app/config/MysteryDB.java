package com.vuong.app.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.sql.Connection;
import java.sql.SQLException;

@Configuration
public class MysteryDB {
    @Bean
    public Connection getConnection() throws SQLException {
        HikariConfig config = new HikariConfig("user-service/src/main/resources/hikari.properties");
        HikariDataSource ds = new HikariDataSource(config);
        return ds.getConnection();
    }
}
