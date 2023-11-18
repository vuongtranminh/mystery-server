package com.vuong.app.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.MacAlgorithm;
import lombok.Builder;
import lombok.Data;
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

    private final KeyUtils keyUtils;
    private MacAlgorithm algAccessToken;
    private SecretKey keyAccessToken;
    private MacAlgorithm algRefreshToken;
    private SecretKey keyRefreshToken;

    public TokenProvider(KeyUtils keyUtils) {
        this.keyUtils = keyUtils;

        this.algAccessToken = Jwts.SIG.HS512;
        this.keyAccessToken = keyUtils.getAccessTokenKey();

        this.algRefreshToken = Jwts.SIG.HS512;
        this.keyRefreshToken = keyUtils.getRefreshTokenKey();
    }

    public String extractUserIdFromAccessToken(String token) {
        return extractSubject(token, keyAccessToken);
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

    public boolean validateToken(String token, SecretKey key) {
        try {
            Jwts.parser().verifyWith(key).build().parseSignedClaims(token);
            return true;
        } catch (JwtException e) {
            log.error("don't trust the JWT!");
        }
        return false;
    }

}

