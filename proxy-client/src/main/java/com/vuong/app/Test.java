package com.vuong.app;

import com.vuong.app.security.TokenProvider;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.io.Encoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.MacAlgorithm;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.UUID;

public class Test {
    public static void main(String[] args) {
        MacAlgorithm algRefreshToken = Jwts.SIG.HS512;
        SecretKey keyRefreshToken = algRefreshToken.key().build();

        String secretString = Encoders.BASE64.encode(keyRefreshToken.getEncoded());
        System.out.println(secretString);

        SecretKey b = getSignKey(secretString);
        System.out.println(Encoders.BASE64.encode(b.getEncoded()));

        String accessToken = Jwts.builder()
                .header()
                .add("typ", "JWT")
                .and()
                .issuer("mystery-server")
                .subject("!")
                .audience()
                .add("mystery-client")
                .and()
                .id("1")
                .signWith(keyRefreshToken, algRefreshToken)
                .compact();

        String bToken = Jwts.builder()
                .header()
                .add("typ", "JWT")
                .and()
                .issuer("mystery-server")
                .subject("!")
                .audience()
                .add("mystery-client")
                .and()
                .id("1")
                .signWith(b, algRefreshToken)
                .compact();

        System.out.println(accessToken.equals(bToken));
    }

    public static SecretKey getSignKey(String secretString) {
        byte[] keyBytes = Decoders.BASE64.decode(secretString);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
