package com.vuong.app.business.auth.controller;

import com.vuong.app.redis.doman.AuthMetadata;
import com.vuong.app.redis.doman.TokenStore;
import com.vuong.app.redis.repository.ManagerAuthSessionRepository;
import com.vuong.app.util.ServletHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.time.Instant;

@RestController
@RequestMapping(path = "/token")
@RequiredArgsConstructor
public class TokenController {

    private  final ManagerAuthSessionRepository managerAuthSessionRepository;

    @PostMapping("/storeToken")
    public String storeToken(HttpServletRequest request) {
        String remoteAddr = ServletHelper.extractIp(request);
        String userAgent = ServletHelper.getUserAgent(request);

        TokenStore tokenStore = TokenStore.builder()
                .accessToken("abc")
                .refreshToken("bcd")
                .build();

        AuthMetadata authMetadata = AuthMetadata.builder()
                .userId("1")
                .userAgent(userAgent)
                .remoteAddr(remoteAddr)
                .lastLoggedIn(Instant.now())
                .build();
        managerAuthSessionRepository.storeToken(tokenStore, authMetadata);

        return "Success";
    }
}
