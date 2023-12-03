package com.vuong.app.security;

import com.vuong.app.business.auth.model.RefreshTokenStatus;
import com.vuong.app.config.AppProperties;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.MacAlgorithm;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;
import java.util.function.Function;

@Service
@Slf4j
public class TokenProvider {

    private final AppProperties appProperties;
    private final KeyUtils keyUtils;
    private MacAlgorithm algAccessToken;
    private SecretKey keyAccessToken;
    private MacAlgorithm algRefreshToken;
    private SecretKey keyRefreshToken;

    public TokenProvider(AppProperties appProperties, KeyUtils keyUtils) {
        this.appProperties = appProperties;
        this.keyUtils = keyUtils;

        this.algAccessToken = Jwts.SIG.HS512;
        this.keyAccessToken = keyUtils.getAccessTokenKey();

        this.algRefreshToken = Jwts.SIG.HS512;
        this.keyRefreshToken = keyUtils.getRefreshTokenKey();
    }

    public AccessToken generateAccessToken(String userId) {
        Date now = new Date();
//        Date expiryDate = new Date(now.getTime() + appProperties.getAuth().getAccessTokenExpirationMsec());
        Date expiryDate = new Date(now.getTime() + 1000 * 30); // 30s

//        String accessToken = Jwts.builder()
//                .setSubject(userId)
//                .setIssuedAt(now)
//                .setExpiration(expiryDate)
//                .signWith(SignatureAlgorithm.HS256, appProperties.getAuth().getAccessTokenSecret())
//                .compact();
        // Create a test key suitable for the desired HMAC-SHA algorithm:

        String accessToken = Jwts.builder()
                .header()
                .add("typ", "JWT")
                .and()
                .issuer("mystery-server")
                .subject(userId)
                .audience()
                .add("mystery-client")
                .and()
                .expiration(expiryDate) //a java.util.Date
                .notBefore(now) //a java.util.Date
                .issuedAt(now) // for example, now
                .id(UUID.randomUUID().toString())
                .signWith(keyAccessToken, algAccessToken)
                .compact();

        return AccessToken.builder()
                .accessToken(accessToken)
                .expiresAt(expiryDate.toInstant())
                .build();
    }

    public RefreshToken generateRefreshToken(String userId) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + appProperties.getAuth().getRefreshTokenExpirationMsec());

//        String refreshToken = Jwts.builder()
//                .setSubject(userId)
//                .setIssuedAt(new Date())
//                .setExpiration(expiryDate)
//                .signWith(SignatureAlgorithm.HS256, appProperties.getAuth().getRefreshTokenSecret())
//                .compact();

        String refreshToken = Jwts.builder()
                .header()
                .add("typ", "JWT")
                .and()
                .issuer("mystery-server")
                .subject(userId)
                .audience()
                .add("mystery-client")
                .and()
                .expiration(expiryDate) //a java.util.Date
                .notBefore(now) //a java.util.Date
                .issuedAt(now) // for example, now
                .id(UUID.randomUUID().toString())
                .signWith(keyRefreshToken, algRefreshToken)
                .compact();

        return RefreshToken.builder()
                .refreshToken(refreshToken)
                .expiresAt(expiryDate.toInstant())
                .userId(userId)
                .status(RefreshTokenStatus.READY)
                .build();
    }

    public RefreshToken generateNewRefreshToken(String oldRefreshToken) {
        String userId = extractUserIdFromRefreshToken(oldRefreshToken);
        Date now = new Date();
        Date expiryDate = extractExpirationFromRefreshToken(oldRefreshToken);

        String refreshToken = Jwts.builder()
                .header()
                .add("typ", "JWT")
                .and()
                .issuer("mystery-server")
                .subject(userId)
                .audience()
                .add("mystery-client")
                .and()
                .expiration(expiryDate) //a java.util.Date
                .notBefore(now) //a java.util.Date
                .issuedAt(now) // for example, now
                .id(UUID.randomUUID().toString())
                .signWith(keyRefreshToken, algRefreshToken)
                .compact();

        return RefreshToken.builder()
                .refreshToken(refreshToken)
                .expiresAt(expiryDate.toInstant())
                .userId(userId)
                .status(RefreshTokenStatus.READY)
                .build();
    }

    @Data
    @Builder
    public static final class AccessToken {
        private String tokenType = "Bearer";
        private String accessToken;
        private Instant expiresAt;
    }

    @Data
    @Builder
    public static final class RefreshToken {
        private String refreshToken;
        private Instant expiresAt;
        private String userId;
        private RefreshTokenStatus status;
    }

    public String extractUserIdFromAccessToken(String token) {
        return extractSubject(token, keyAccessToken);
    }

    public String extractUserIdFromRefreshToken(String token) {
        return extractSubject(token, keyRefreshToken);
    }

    public Date extractExpirationFromRefreshToken(String token) {
        return extractExpiration(token, keyRefreshToken);
    }

    private Boolean isTokenExpired(String token, SecretKey key) {
        return extractExpiration(token, key).before(new Date());
    }

    public String extractSubject(String token, SecretKey key) {
        return extractClaim(token, key, Claims::getSubject);
    }

    public Date extractExpiration(String token, SecretKey key) {
        return extractClaim(token, key, Claims::getExpiration);
    }

    public <T> T extractClaim(String token, SecretKey key, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token, key);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token, SecretKey key) {
        return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
    }

    public boolean validateAccessToken(String token) {
        return this.validateToken(token, this.keyAccessToken);
    }

    public boolean validateRefreshToken(String token) {
        return this.validateToken(token, this.keyRefreshToken);
    }

    public boolean validateToken(String token, SecretKey key) {
        try {
            Jwts.parser().verifyWith(key).build().parseSignedClaims(token);
            return true;
        } catch (JwtException e) {
            log.error("don't trust the JWT!");
        }

//        try {
//            MacAlgorithm alg = Jwts.SIG.HS512; //or HS384 or HS256
//            SecretKey key = convertStringToSecretKeyto(appProperties.getAuth().getAccessTokenSecret());
////            Jwts.parser().setSigningKey(appProperties.getAuth().getAccessTokenSecret()).parseClaimsJws(token);
//            Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
//            return true;
//        } catch (SignatureException ex) {
//            log.error("Invalid JWT signature");
//        } catch (MalformedJwtException ex) {
//            log.error("Invalid JWT token");
//        } catch (ExpiredJwtException ex) {
//            log.error("Expired JWT token");
//        } catch (UnsupportedJwtException ex) {
//            log.error("Unsupported JWT token");
//        } catch (IllegalArgumentException ex) {
//            log.error("JWT claims string is empty.");
//        }
        return false;
    }

}

