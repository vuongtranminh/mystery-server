package com.vuong.app.security.oauth2;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vuong.app.business.auth.model.*;
import com.vuong.app.exception.wrapper.ResourceNotFoundException;
import com.vuong.app.grpc.service.AuthClientService;
import com.vuong.app.exception.wrapper.OAuth2AuthenticationProcessingException;
import com.vuong.app.grpc.message.auth.*;
import com.vuong.app.security.UserPrincipal;
import com.vuong.app.security.oauth2.user.OAuth2UserInfo;
import com.vuong.app.security.oauth2.user.OAuth2UserInfoFactory;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.*;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final AuthClientService authClientService;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest oAuth2UserRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(oAuth2UserRequest);

        try {
            return processOAuth2User(oAuth2UserRequest, oAuth2User);
        } catch (AuthenticationException ex) {
            throw ex;
        } catch (Exception ex) {
            // Throwing an instance of AuthenticationException will trigger the OAuth2AuthenticationFailureHandler
            throw new InternalAuthenticationServiceException(ex.getMessage(), ex.getCause());
        }
    }

    private OAuth2User processOAuth2User(OAuth2UserRequest oAuth2UserRequest, OAuth2User oAuth2User) {
        OAuth2UserInfo oAuth2UserInfo = OAuth2UserInfoFactory.getOAuth2UserInfo(oAuth2UserRequest.getClientRegistration().getRegistrationId(), oAuth2User.getAttributes());
        if (StringUtils.isBlank(oAuth2UserInfo.getEmail())) {
            if (oAuth2UserRequest.getClientRegistration().getRegistrationId().equalsIgnoreCase("github")) {
                oAuth2UserInfo.setEmail(requestEmail(oAuth2UserRequest.getAccessToken().getTokenValue()));
            } else {
                throw new OAuth2AuthenticationProcessingException("Email not found from OAuth2 provider");
            }
        }

        Optional<GetUserByEmailResponse> userOptional = this.authClientService.getUserByEmail(GetUserByEmailRequest.builder()
                .email(oAuth2UserInfo.getEmail())
                .build());
        UserDto user;
        if(userOptional.isPresent()) {
            user = userOptional.get();
            if(!user.getProvider().equals(AuthProvider.valueOf(oAuth2UserRequest.getClientRegistration().getRegistrationId()))) {
                throw new OAuth2AuthenticationProcessingException("Looks like you're signed up with " +
                        user.getProvider() + " account. Please use your " + user.getProvider() +
                        " account to login.");
            }

            GetUserPrincipalByUserIdResponse userPrincipalByUserIdResponse = updateExistingUser(user, oAuth2UserInfo);
            return UserPrincipal.create(userPrincipalByUserIdResponse, oAuth2User.getAttributes());
        }

        GetUserPrincipalByUserIdResponse userPrincipalByUserIdResponse = registerNewUser(oAuth2UserRequest, oAuth2UserInfo);
        return UserPrincipal.create(userPrincipalByUserIdResponse, oAuth2User.getAttributes());
    }

    private GetUserPrincipalByUserIdResponse registerNewUser(OAuth2UserRequest oAuth2UserRequest, OAuth2UserInfo oAuth2UserInfo) {
        CreateUserSocialResponse createUserSocialResponse = this.authClientService.createUserSocial(CreateUserSocialRequest.builder()
                .name(oAuth2UserInfo.getName())
                .avtUrl(oAuth2UserInfo.getImageUrl())
                .email(oAuth2UserInfo.getEmail())
                .provider(AuthProvider.valueOf(oAuth2UserRequest.getClientRegistration().getRegistrationId()))
                .providerId(oAuth2UserInfo.getId())
                .build());

        GetUserPrincipalByUserIdResponse userPrincipalByUserIdResponse = this.authClientService.getUserPrincipalByUserId(GetUserPrincipalByUserIdRequest.builder().userId(createUserSocialResponse.getUserId()).build())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", createUserSocialResponse.getUserId()));

        return userPrincipalByUserIdResponse;
    }

    private GetUserPrincipalByUserIdResponse updateExistingUser(UserDto existingUser, OAuth2UserInfo oAuth2UserInfo) {
        UpdateUserByUserIdResponse updateUserByUserIdResponse = this.authClientService.updateUserByUserIdRequest(UpdateUserByUserIdRequest.builder()
                .userId(existingUser.getUserId())
                .name(oAuth2UserInfo.getName())
                .avtUrl(oAuth2UserInfo.getImageUrl())
                .bio(existingUser.getBio())
                .build());

        GetUserPrincipalByUserIdResponse userPrincipalByUserIdResponse = this.authClientService.getUserPrincipalByUserId(GetUserPrincipalByUserIdRequest.builder().userId(updateUserByUserIdResponse.getUserId()).build())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", updateUserByUserIdResponse.getUserId()));

        return userPrincipalByUserIdResponse;
    }

    private String requestEmail(String token) {
        String url = "https://api.github.com/user/emails";
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "token " + token);
        HttpEntity request = new HttpEntity(headers);
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, request, String.class, 1);

        if (response.getStatusCode() == HttpStatus.OK) {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

            List<GithubEmailResponse> emails;
            try {
                emails = objectMapper.readValue(response.getBody(), new TypeReference<>() {});
            } catch (JsonProcessingException ex) {
                throw new OAuth2AuthenticationProcessingException("Email not found from OAuth2 provider");
            }

            String primaryEmail = "";
            for(GithubEmailResponse email: emails)
                if (email.isPrimary()) {
                    primaryEmail = email.getEmail();
                    break;
                }
            return primaryEmail;
        } else {
            throw new OAuth2AuthenticationProcessingException("Email not found from OAuth2 provider");
        }
    }

    private class GithubEmailResponse {
        private String email;
        private boolean primary;
        private boolean verified;
        private String visibility;

        public String getEmail() {
            return email;
        }

        public boolean isPrimary() {
            return primary;
        }

        public boolean isVerified() {
            return verified;
        }

        public String getVisibility() {
            return visibility;
        }
    }

}
