package com.vuong.app.business.auth.controller;

import com.vuong.app.business.auth.model.*;
import com.vuong.app.business.auth.service.AuthService;
import com.vuong.app.config.AppProperties;
import com.vuong.app.grpc.message.auth.CreateRefreshTokenRequest;
import com.vuong.app.grpc.message.auth.CreateRefreshTokenResponse;
import com.vuong.app.grpc.message.auth.CreateUserRequest;
import com.vuong.app.grpc.message.auth.CreateUserResponse;
import com.vuong.app.business.auth.model.payload.LoginRequest;
import com.vuong.app.business.auth.model.payload.SignUpRequest;
import com.vuong.app.grpc.service.AuthClientService;
import com.vuong.app.exception.wrapper.BadRequestException;
import com.vuong.app.security.TokenProvider;
import com.vuong.app.security.UserPrincipal;
import com.vuong.app.util.CookieUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.net.URI;
import java.util.Optional;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;

    private final PasswordEncoder passwordEncoder;

    private final TokenProvider tokenProvider;

    private final AuthService authService;

    private final AppProperties appProperties;

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest, HttpServletResponse response) {

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getEmail(),
                        loginRequest.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

        TokenProvider.AccessToken accessToken = tokenProvider.generateAccessToken(userPrincipal.getUserId());
        TokenProvider.RefreshToken refreshToken = tokenProvider.generateRefreshToken(userPrincipal.getUserId());

        // save refresh to db
//        Optional<CreateRefreshTokenResponse> createRefreshTokenResponseOptional = this.authClientService.createRefreshToken(CreateRefreshTokenRequest.builder()
//                .refreshToken(refreshToken.getRefreshToken())
//                .expiresAt(refreshToken.getExpiresAt())
//                .userId(refreshToken.getUserId())
//                .status(refreshToken.getStatus())
//                .build());
//
//        if (!createRefreshTokenResponseOptional.isPresent()) {
//            // throw Create refresh token error
//        }

        CookieUtils.addCookie(response, appProperties.getAuth().getAccessTokenCookieName(), accessToken.getAccessToken(), (int) appProperties.getAuth().getAccessTokenExpirationMsec());
        CookieUtils.addCookie(response, appProperties.getAuth().getRefreshTokenCookieName(), refreshToken.getRefreshToken(), (int) appProperties.getAuth().getRefreshTokenExpirationMsec());

        return ResponseEntity.ok("OK");
    }

    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignUpRequest signUpRequest) {
        if(this.authService.existsByEmail(signUpRequest.getEmail())) {
            throw new BadRequestException("Email address already in use.");
        }

        Integer userId = this.authService.create(signUpRequest);

        // Creating user's account

        URI location = ServletUriComponentsBuilder
                .fromCurrentContextPath().path("/user/me")
                .buildAndExpand(userId).toUri();

        return ResponseEntity.created(location)
                .body("User registered successfully@");
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request, HttpServletResponse response) {
        // update status refesh token to DISABLE

        CookieUtils.deleteCookie(request, response, appProperties.getAuth().getAccessTokenCookieName());
        CookieUtils.deleteCookie(request, response, appProperties.getAuth().getRefreshTokenCookieName());

        return ResponseEntity.ok("OK");
    }

    @PostMapping("/refeshToken")
    public ResponseEntity<?> refeshToken(HttpServletRequest request, HttpServletResponse response) {
        Optional<Cookie> oldRefreshTokenOptional = CookieUtils.getCookie(request, appProperties.getAuth().getRefreshTokenCookieName());
        if (!oldRefreshTokenOptional.isPresent()) {
            // throw error to /login
        }

        String oldRefreshToken = oldRefreshTokenOptional.get().getName();

        // extract oldRefreshToken get userId and status in db
        // if status != ready => delete all ccookie and return to /login
        // maybe add notification to user hacked refresh token



        TokenProvider.RefreshToken refreshToken = tokenProvider.generateRefreshToken(oldRefreshToken);
        // update refresh token to status used
        // insert refresh token to DB
        TokenProvider.AccessToken accessToken = tokenProvider.generateAccessToken(refreshToken.getUserId());

        CookieUtils.addCookie(response, appProperties.getAuth().getAccessTokenCookieName(), accessToken.getAccessToken(), (int) appProperties.getAuth().getAccessTokenExpirationMsec());
        CookieUtils.addCookie(response, appProperties.getAuth().getRefreshTokenCookieName(), refreshToken.getRefreshToken(), (int) appProperties.getAuth().getRefreshTokenExpirationMsec());

        return ResponseEntity.ok("OK");
    }
}
