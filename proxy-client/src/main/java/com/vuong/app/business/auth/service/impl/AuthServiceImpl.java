package com.vuong.app.business.auth.service.impl;

import com.vuong.app.business.auth.model.AuthProvider;
import com.vuong.app.business.auth.model.RefreshTokenStatus;
import com.vuong.app.business.auth.model.payload.*;
import com.vuong.app.business.auth.service.AuthService;
import com.vuong.app.common.api.ExceptionMsg;
import com.vuong.app.common.api.ResponseMsg;
import com.vuong.app.common.api.ResponseObject;
import com.vuong.app.config.AppProperties;
import com.vuong.app.exception.wrapper.BadRequestException;
import com.vuong.app.grpc.message.auth.*;
import com.vuong.app.grpc.service.AuthClientService;
import com.vuong.app.grpc.service.UserClientService;
import com.vuong.app.redis.doman.AuthMetadata;
import com.vuong.app.redis.doman.TokenStore;
import com.vuong.app.redis.repository.ManagerAuthSessionRepository;
import com.vuong.app.security.TokenProvider;
import com.vuong.app.security.UserPrincipal;
import com.vuong.app.util.CookieUtils;
import com.vuong.app.util.ServletHelper;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthClientService authClientService;

    private final AuthenticationManager authenticationManager;

    private final PasswordEncoder passwordEncoder;

    private final TokenProvider tokenProvider;

    private final AppProperties appProperties;

    private final ManagerAuthSessionRepository managerAuthSessionRepository;

    private final UserClientService userClientService;

//    @Override
//    public boolean existsByEmail(String email) {
//        ExistsUserByEmailResponse existsUserByEmailResponse = this.authClientService.existsUserByEmail(ExistsUserByEmailRequest.builder()
//                .email(email)
//                .build());
//        return existsUserByEmailResponse.isExists();
//    }

    @Override
    public ResponseObject signIn(HttpServletRequest request, HttpServletResponse response, SignInRequest signInRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        signInRequest.getEmail(),
                        signInRequest.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

        TokenProvider.AccessToken accessToken = tokenProvider.generateAccessToken(userPrincipal.getUserId());
        TokenProvider.RefreshToken refreshToken = tokenProvider.generateRefreshToken(userPrincipal.getUserId());

        String remoteAddr = ServletHelper.extractIp(request);
        String userAgent = ServletHelper.getUserAgent(request);

        TokenStore tokenStore = TokenStore.builder()
                .accessToken(accessToken.getAccessToken())
                .refreshToken(refreshToken.getRefreshToken())
                .build();

        AuthMetadata authMetadata = AuthMetadata.builder()
                .userId(userPrincipal.getUserId())
                .userAgent(userAgent)
                .remoteAddr(remoteAddr)
                .lastLoggedIn(Instant.now())
                .build();
        managerAuthSessionRepository.storeToken(tokenStore, authMetadata);

        CookieUtils.addCookie(response, appProperties.getAuth().getAccessTokenCookieName(), CookieUtils.serialize(accessToken.getAccessToken()), appProperties.getAuth().getAccessTokenExpirationMsec());
        CookieUtils.addCookie(response, appProperties.getAuth().getRefreshTokenCookieName(), CookieUtils.serialize(refreshToken.getRefreshToken()), appProperties.getAuth().getRefreshTokenExpirationMsec());

        return new ResponseMsg("Sign in successfully!", HttpStatus.OK);
    }

    @Override
    public ResponseObject signUp(SignUpRequest signUpRequest) {
        ExistsUserByEmailResponse existsUserByEmailResponse = this.userClientService.existsUserByEmail(ExistsUserByEmailRequest.builder()
                .email(signUpRequest.getEmail())
                .build());
        if (existsUserByEmailResponse.isExists()) {
            throw new BadRequestException("Email address already in use.");
        }

        CreateUserLocalResponse createUserLocalResponse = this.authClientService.createUserLocal(CreateUserLocalRequest.builder()
                .name(signUpRequest.getName())
                .email(signUpRequest.getEmail())
                .password(passwordEncoder.encode(signUpRequest.getPassword()))
                .build());

        return new ResponseMsg("Sign up successfully!", HttpStatus.OK, new SignUpResponse(createUserLocalResponse.getUserId()));
    }

    @Override
    public ResponseObject logout(HttpServletRequest request, HttpServletResponse response) {
        String userAgent = ServletHelper.getUserAgent(request);

        String oldRefreshToken = CookieUtils.getCookie(request, appProperties.getAuth().getRefreshTokenCookieName())
                .map(cookie -> CookieUtils.deserialize(cookie, String.class))
                .orElse(null);

        if (StringUtils.isBlank(oldRefreshToken)) {
            return new ExceptionMsg("Refresh token not found", HttpStatus.NOT_FOUND);
        }

        String userId = tokenProvider.extractUserIdFromRefreshToken(oldRefreshToken);

        AuthMetadata authMetadata = AuthMetadata.builder()
                .userId(userId)
                .userAgent(userAgent)
                .build();

        managerAuthSessionRepository.removeTokenByAuthMetadata(userId, authMetadata);

        CookieUtils.deleteCookie(request, response, appProperties.getAuth().getAccessTokenCookieName());
        CookieUtils.deleteCookie(request, response, appProperties.getAuth().getRefreshTokenCookieName());

        response.addHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS, Boolean.TRUE.toString());
        response.addHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS, "GET,PUT,POST,DELETE,UPDATE,OPTIONS");
        response.addHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "http://localhost:3000,http://localhost:8080,http://localhost:8900");
        response.addHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS, "X-Requested-With, X-HTTP-Method-Override, Content-Type, Accept");
        return new ResponseMsg("Log out successfully!", HttpStatus.OK);
    }

    @Override
    public ResponseObject refeshToken(HttpServletRequest request, HttpServletResponse response) {
        String oldRefreshToken = CookieUtils.getCookie(request, appProperties.getAuth().getRefreshTokenCookieName())
                .map(cookie -> CookieUtils.deserialize(cookie, String.class))
                .orElse(null);

        if (StringUtils.isBlank(oldRefreshToken)) {
            return new ExceptionMsg("Refresh token not found", HttpStatus.NOT_FOUND);
        }

        if (managerAuthSessionRepository.hasRefreshToken(oldRefreshToken) || !tokenProvider.validateRefreshToken(oldRefreshToken)) { // pass is expiresAt before now
            return new ExceptionMsg("Invalid refresh token", HttpStatus.BAD_REQUEST);
        }

        TokenProvider.RefreshToken refreshToken = tokenProvider.generateRefreshToken(oldRefreshToken);
        // update refresh token to status used
        TokenProvider.AccessToken accessToken = tokenProvider.generateAccessToken(refreshToken.getUserId());

        String remoteAddr = ServletHelper.extractIp(request);
        String userAgent = ServletHelper.getUserAgent(request);

        TokenStore tokenStore = TokenStore.builder()
                .accessToken(accessToken.getAccessToken())
                .refreshToken(refreshToken.getRefreshToken())
                .build();

        AuthMetadata authMetadata = AuthMetadata.builder()
                .userId(refreshToken.getUserId())
                .userAgent(userAgent)
                .remoteAddr(remoteAddr)
                .lastLoggedIn(Instant.now())
                .build();
        managerAuthSessionRepository.storeToken(tokenStore, authMetadata);

        CookieUtils.addCookie(response, appProperties.getAuth().getAccessTokenCookieName(), CookieUtils.serialize(accessToken.getAccessToken()), appProperties.getAuth().getAccessTokenExpirationMsec());
        CookieUtils.addCookie(response, appProperties.getAuth().getRefreshTokenCookieName(), CookieUtils.serialize(refreshToken.getRefreshToken()), appProperties.getAuth().getRefreshTokenExpirationMsec());

        return new ResponseMsg("Provide new access token successfully!", HttpStatus.OK);
    }

    @Override
    public ResponseObject verificationCredential(boolean isOtp, String verify) {
        if (isOtp) {
            this.authClientService.verificationCredentialByVerificationOtp(VerificationCredentialByVerificationOtpRequest.builder()
                    .verificationOtp(verify)
                    .build());
        } else {
            this.authClientService.verificationCredentialByVerificationToken(VerificationCredentialByVerificationTokenRequest.builder()
                    .verificationToken(verify)
                    .build());
        }

        return new ResponseMsg("Verify credential successfully!", HttpStatus.OK);
    }

    @Override
    public ResponseObject reissueVerificationCredential(ReissueVerificationCredentialRequest request) {
        this.authClientService.reissueVerificationCredentialByUserId(ReissueVerificationCredentialByUserIdRequest.builder()
                .userId(request.getUserId())
                .build());
        return new ResponseMsg("Verify credential successfully!", HttpStatus.OK);
    }

    @Override
    public ResponseObject changeUserPassword(ChangeUserPasswordRequest request) {
        this.authClientService.changeUserPasswordByUserId(ChangeUserPasswordByUserIdRequest.builder()
                .userId(request.getUserId())
                .oldPassword(passwordEncoder.encode(request.getOldPassword()))
                .newPassword(passwordEncoder.encode(request.getNewPassword()))
                .build());
        return new ResponseMsg("Change password successfully!", HttpStatus.OK);
    }

    @Override
    public ResponseObject getAuthMetaData(UserPrincipal currentUser) {
        List<AuthMetadata> result = this.managerAuthSessionRepository.getAuthMetaData(currentUser.getUserId());
        return new ResponseMsg("success", HttpStatus.OK, result);
    }

    @Override
    public ResponseObject removeAuthMetaData(HttpServletRequest request, UserPrincipal currentUser) {
        String userAgent = ServletHelper.getUserAgent(request);

        this.managerAuthSessionRepository.removeAuthMetaData(currentUser.getUserId(), userAgent);

        return new ResponseMsg("remove AuthMetaData successfully!", HttpStatus.OK);
    }
}
