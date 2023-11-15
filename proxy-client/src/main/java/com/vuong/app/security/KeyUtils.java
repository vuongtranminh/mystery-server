package com.vuong.app.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.io.Encoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.MacAlgorithm;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.io.*;
import java.util.Arrays;
import java.util.Objects;

@Component
@Slf4j
@RequiredArgsConstructor
public class KeyUtils {

    private final Environment environment;

    @Value("${access-token.path}")
    private String accessTokenKeyPath;

    @Value("${refresh-token.path}")
    private String refreshTokenKeyPath;

    private SecretKey accessTokenKey;
    private SecretKey refreshTokenKey;

    public SecretKey getAccessTokenKey() {
        if (Objects.isNull(accessTokenKey)) {
            accessTokenKey = getKey(accessTokenKeyPath);
        }
        return accessTokenKey;
    }

    public SecretKey getRefreshTokenKey() {
        if (Objects.isNull(refreshTokenKey)) {
            refreshTokenKey = getKey(refreshTokenKeyPath);
        }
        return refreshTokenKey;
    }

    private SecretKey getKey(String keyPath) {
        SecretKey key;

        File keyFile = new File(keyPath);

        if (keyFile.exists()) {
            log.info("loading keys from file: {}", keyPath);
            try {
                String secretString = readFile(keyFile);
                byte[] keyBytes = Decoders.BASE64.decode(secretString);
                key = Keys.hmacShaKeyFor(keyBytes);
                return key;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } else {
            if (Arrays.stream(environment.getActiveProfiles()).anyMatch(s -> s.equals("prod"))) {
                throw new RuntimeException("public and private keys don't exist");
            }
        }

        File directory = new File(keyPath);
        if (!directory.exists()) {
            directory.mkdirs();
        }
        try {
            log.info("Generating new public and private keys: {}", keyPath);
            MacAlgorithm alg = Jwts.SIG.HS512;
            key = alg.key().build();
            String secretString = Encoders.BASE64.encode(key.getEncoded());

            writeFile(keyFile, secretString);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return key;
    }

    private void writeFile(File file, String msg) {
        try (FileOutputStream fos = new FileOutputStream(file)) {
            // converting string thanh mang byte
            byte byteArray[] = msg.getBytes();
            fos.write(byteArray);
            System.out.println("Thong diep da duoc ghi vao file thanh cong!");
        } catch (Exception exception) {
            System.out.println(exception);
        }
    }

    private String readFile(File file) {
        try (InputStream is = new FileInputStream(file)) {
            DataInputStream inst = new DataInputStream(is);
            int data = is.available();
            byte[] byteArray = new byte[data];
            inst.read(byteArray);
            String str = new String(byteArray);
            System.out.println("Thong diep da duoc doc tu file thanh cong!");
            return str;
        } catch (Exception exception) {
            System.out.println(exception);
        }
        return null;
    }

}
