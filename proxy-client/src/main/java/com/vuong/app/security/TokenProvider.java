package com.vuong.app.security;

import com.vuong.app.business.auth.model.RefreshTokenStatus;
import com.vuong.app.config.AppProperties;
import io.jsonwebtoken.*;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.UUID;
import java.util.function.Function;

@Service
@Slf4j
@RequiredArgsConstructor
public class TokenProvider {

    private final AppProperties appProperties;

    public AccessToken generateAccessToken(Integer userId) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + appProperties.getAuth().getAccessTokenSecret());

        String accessToken = Jwts.builder()
                .setSubject(Integer.toString(userId))
                .setIssuedAt(new Date())
                .setExpiration(expiryDate)
                .signWith(SignatureAlgorithm.HS256, appProperties.getAuth().getAccessTokenSecret())
                .compact();

        return AccessToken.builder()
                .accessToken(accessToken)
                .expiresAt(expiryDate.toString())
                .build();
    }

    public RefreshToken generateRefreshToken(Integer userId) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + appProperties.getAuth().getRefreshTokenExpirationMsec());

        String refreshToken = Jwts.builder()
                .setSubject(Integer.toString(userId))
                .setIssuedAt(new Date())
                .setExpiration(expiryDate)
                .signWith(SignatureAlgorithm.HS256, appProperties.getAuth().getRefreshTokenSecret())
                .compact();

        return RefreshToken.builder()
                .refreshToken(refreshToken)
                .expiresAt(expiryDate.toString())
                .userId(userId)
                .status(RefreshTokenStatus.READY)
                .build();
    }

    public RefreshToken generateRefreshToken(String oldRefreshToken) {
        if (!validateToken(oldRefreshToken)) {
            // throw exception
        }

        Integer userId = extractUserIdFromRefreshToken(oldRefreshToken);
        Date expiryDate = extractExpirationFromRefreshToken(oldRefreshToken);

        String refreshToken = Jwts.builder()
                .setSubject(Integer.toString(userId))
                .setIssuedAt(new Date())
                .setExpiration(expiryDate)
                .signWith(SignatureAlgorithm.HS256, appProperties.getAuth().getRefreshTokenSecret())
                .compact();

        return RefreshToken.builder()
                .refreshToken(refreshToken)
                .expiresAt(expiryDate.toString())
                .userId(userId)
                .status(RefreshTokenStatus.READY)
                .build();
    }

    @Data
    @Builder
    public static class AccessToken {
        private String tokenType = "Bearer";
        private String accessToken;
        private String expiresAt;
    }

    @Data
    @Builder
    public static class RefreshToken {
        private String refreshToken;
        private String expiresAt;
        private Integer userId;
        private RefreshTokenStatus status;
    }

    public Integer extractUserIdFromAccessToken(String token) {
        return Integer.parseInt(extractSubject(token, appProperties.getAuth().getAccessTokenSecret()));
    }

    public Integer extractUserIdFromRefreshToken(String token) {
        return Integer.parseInt(extractSubject(token, appProperties.getAuth().getRefreshTokenSecret()));
    }

    public Integer getUserIdFromRefreshToken(String token) {
        return Integer.parseInt(extractSubject(token, appProperties.getAuth().getRefreshTokenSecret()));
    }

    public Date extractExpirationFromRefreshToken(String token) {
        return extractExpiration(token, appProperties.getAuth().getRefreshTokenSecret());
    }

    private Boolean isTokenExpired(String token, String secretKey) {
        return extractExpiration(token, secretKey).before(new Date());
    }

    public String extractSubject(String token, String secretKey) {
        return extractClaim(token, secretKey, Claims::getSubject);
    }

    public Date extractExpiration(String token, String secretKey) {
        return extractClaim(token, secretKey, Claims::getExpiration);
    }

    public <T> T extractClaim(String token, String secretKey, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token, secretKey);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token, String secretKey) {
        return Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token).getBody();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser().setSigningKey(appProperties.getAuth().getAccessTokenSecret()).parseClaimsJws(token);
            return true;
        } catch (SignatureException ex) {
            log.error("Invalid JWT signature");
        } catch (MalformedJwtException ex) {
            log.error("Invalid JWT token");
        } catch (ExpiredJwtException ex) {
            log.error("Expired JWT token");
        } catch (UnsupportedJwtException ex) {
            log.error("Unsupported JWT token");
        } catch (IllegalArgumentException ex) {
            log.error("JWT claims string is empty.");
        }
        return false;
    }

}

