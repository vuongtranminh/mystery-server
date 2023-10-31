package com.vuong.app.security;

import com.vuong.app.business.auth.model.RefreshTokenStatus;
import com.vuong.app.config.AppProperties;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.MacAlgorithm;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.UUID;
import java.util.function.Function;

@Service
@Slf4j
@RequiredArgsConstructor
public class TokenProvider {

    private final AppProperties appProperties;

    public static SecretKey convertStringToSecretKeyto(String encodedKey) {
        byte[] decodedKey = Base64.getDecoder().decode(encodedKey);
        SecretKey originalKey = new SecretKeySpec(decodedKey, 0, decodedKey.length, "AES");
        return originalKey;
    }

    public AccessToken generateAccessToken(String userId) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + appProperties.getAuth().getAccessTokenExpirationMsec());

//        String accessToken = Jwts.builder()
//                .setSubject(userId)
//                .setIssuedAt(now)
//                .setExpiration(expiryDate)
//                .signWith(SignatureAlgorithm.HS256, appProperties.getAuth().getAccessTokenSecret())
//                .compact();
        MacAlgorithm alg = Jwts.SIG.HS512; //or HS384 or HS256
        SecretKey key = convertStringToSecretKeyto(appProperties.getAuth().getAccessTokenSecret());

        String accessToken = Jwts.builder()
                .header()
                .add("typ", "JWT")
                .and()
                .issuer("mystery-server")
                .subject("Bob")
                .audience()
                .add("mystery-client")
                .and()
                .expiration(expiryDate) //a java.util.Date
                .notBefore(now) //a java.util.Date
                .issuedAt(now) // for example, now
                .id(UUID.randomUUID().toString())
                .signWith(key, alg)
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

        MacAlgorithm alg = Jwts.SIG.HS512; //or HS384 or HS256
        SecretKey key = convertStringToSecretKeyto(appProperties.getAuth().getAccessTokenSecret());

        String refreshToken = Jwts.builder()
                .header()
                .add("typ", "JWT")
                .and()
                .issuer("mystery-server")
                .subject("Bob")
                .audience()
                .add("mystery-client")
                .and()
                .expiration(expiryDate) //a java.util.Date
                .notBefore(now) //a java.util.Date
                .issuedAt(now) // for example, now
                .id(UUID.randomUUID().toString())
                .signWith(key, alg)
                .compact();

        return RefreshToken.builder()
                .refreshToken(refreshToken)
                .expiresAt(expiryDate.toInstant())
                .userId(userId)
                .status(RefreshTokenStatus.READY)
                .build();
    }

//    public RefreshToken generateRefreshToken(String oldRefreshToken) {
//        if (!validateToken(oldRefreshToken)) {
//            // throw exception
//        }
//
//        String userId = extractUserIdFromRefreshToken(oldRefreshToken);
//        Date expiryDate = extractExpirationFromRefreshToken(oldRefreshToken);
//
//        String refreshToken = Jwts.builder()
//                .setSubject(userId)
//                .setIssuedAt(new Date())
//                .setExpiration(expiryDate)
//                .signWith(SignatureAlgorithm.HS256, appProperties.getAuth().getRefreshTokenSecret())
//                .compact();
//
//        return RefreshToken.builder()
//                .refreshToken(refreshToken)
//                .expiresAt(expiryDate.toInstant())
//                .userId(userId)
//                .status(RefreshTokenStatus.READY)
//                .build();
//    }

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
        return extractSubject(token, appProperties.getAuth().getAccessTokenSecret());
    }

    public String extractUserIdFromRefreshToken(String token) {
        return extractSubject(token, appProperties.getAuth().getRefreshTokenSecret());
    }

    public String getUserIdFromRefreshToken(String token) {
        return extractSubject(token, appProperties.getAuth().getRefreshTokenSecret());
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
        MacAlgorithm alg = Jwts.SIG.HS512; //or HS384 or HS256
        SecretKey key = convertStringToSecretKeyto(appProperties.getAuth().getAccessTokenSecret());
        return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
    }

    public boolean validateToken(String token) {
        MacAlgorithm alg = Jwts.SIG.HS512; //or HS384 or HS256
        SecretKey key = convertStringToSecretKeyto(appProperties.getAuth().getAccessTokenSecret());
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

