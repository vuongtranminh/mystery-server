package com.vuong.app.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

@ConfigurationProperties(prefix = "app")
@Getter
@Setter
public class AppProperties {
    private final Auth auth = new Auth();

    @Getter
    @Setter
    public static class Auth {
        private String accessTokenCookieName;
    }

}
