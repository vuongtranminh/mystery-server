package com.vuong.app.config;

import com.vuong.app.jdbc.SqlSession;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
public class MysteryDB {
//    @Bean
//    public DataSource dataSource() {
//        HikariConfig config = new HikariConfig("discord-service/src/main/resources/hikari.properties");
//        DataSource ds = new HikariDataSource(config);
//        return ds;
//    }

    @Bean
    @ConfigurationProperties("spring.datasource.hikari")
    public DataSource dataSource() {
//        HikariConfig config = new HikariConfig("src/main/resources/hikari.properties");
//        DataSource ds = new HikariDataSource(config);
        return DataSourceBuilder.create().type(HikariDataSource.class).build();
//        return ds;
    }

    @Bean
    public SqlSession sqlSession(DataSource dataSource) {
        return new SqlSession(dataSource);
    }
}
