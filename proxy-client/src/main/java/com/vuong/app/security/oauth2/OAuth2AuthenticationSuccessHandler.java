package com.vuong.app.security.oauth2;

import com.vuong.app.config.AppProperties;
import com.vuong.app.exception.wrapper.BadRequestException;
import com.vuong.app.grpc.message.auth.CreateRefreshTokenRequest;
import com.vuong.app.grpc.message.auth.CreateRefreshTokenResponse;
import com.vuong.app.grpc.service.AuthClientService;
import com.vuong.app.security.TokenProvider;
import com.vuong.app.security.UserPrincipal;
import com.vuong.app.util.CookieUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;
import java.util.Optional;

import static com.vuong.app.security.oauth2.HttpCookieOAuth2AuthorizationRequestRepository.REDIRECT_URI_PARAM_COOKIE_NAME;

@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final TokenProvider tokenProvider;

    private final AppProperties appProperties;

    private final HttpCookieOAuth2AuthorizationRequestRepository httpCookieOAuth2AuthorizationRequestRepository;

    private final AuthClientService authClientService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        String targetUrl = determineTargetUrl(request, response, authentication);

        if (response.isCommitted()) {
            logger.debug("Response has already been committed. Unable to redirect to " + targetUrl);
            return;
        }

        clearAuthenticationAttributes(request, response);
        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }

    protected String determineTargetUrl(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        Optional<String> redirectUri = CookieUtils.getCookie(request, REDIRECT_URI_PARAM_COOKIE_NAME)
                .map(Cookie::getValue);

        if(redirectUri.isPresent() && !isAuthorizedRedirectUri(redirectUri.get())) {
            throw new BadRequestException("Sorry! We've got an Unauthorized Redirect URI and can't proceed with the authentication");
        }

        String targetUrl = redirectUri.orElse(getDefaultTargetUrl());

        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

        TokenProvider.AccessToken accessToken = tokenProvider.generateAccessToken(userPrincipal.getUserId());
        TokenProvider.RefreshToken refreshToken = tokenProvider.generateRefreshToken(userPrincipal.getUserId());

        this.authClientService.createRefreshToken(CreateRefreshTokenRequest.builder()
                .refreshToken(refreshToken.getRefreshToken())
                .expiresAt(refreshToken.getExpiresAt())
                .userId(refreshToken.getUserId())
                .build());

        CookieUtils.addCookie(response, appProperties.getAuth().getAccessTokenCookieName(), CookieUtils.serialize(accessToken.getAccessToken()), (int) appProperties.getAuth().getAccessTokenExpirationMsec());
        CookieUtils.addCookie(response, appProperties.getAuth().getRefreshTokenCookieName(), CookieUtils.serialize(refreshToken.getRefreshToken()), (int) appProperties.getAuth().getRefreshTokenExpirationMsec());

//        return UriComponentsBuilder.fromUriString("http://localhost:3000")
//                .queryParam("token", token)
//                .build().toUriString();
        return UriComponentsBuilder.fromUriString(appProperties.getOauth2().getAuthorizedRedirectSuccessUri())
//                .queryParam("token", token)
                .build().toUriString();
    }

    protected void clearAuthenticationAttributes(HttpServletRequest request, HttpServletResponse response) {
        super.clearAuthenticationAttributes(request);
        httpCookieOAuth2AuthorizationRequestRepository.removeAuthorizationRequestCookies(request, response);
    }

    private boolean isAuthorizedRedirectUri(String uri) {
        URI clientRedirectUri = URI.create(uri);

        return appProperties.getOauth2().getAuthorizedRedirectUris()
                .stream()
                .anyMatch(authorizedRedirectUri -> {
                    // Only validate host and port. Let the clients use different paths if they want to
                    URI authorizedURI = URI.create(authorizedRedirectUri);
                    if(authorizedURI.getHost().equalsIgnoreCase(clientRedirectUri.getHost())
                            && authorizedURI.getPort() == clientRedirectUri.getPort()) {
                        return true;
                    }
                    return false;
                });
    }
}

