package com.vuong.app.redis.doman;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.Instant;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class AuthMetadata implements Serializable {
    private String userId;
    private String userAgent;
    private String remoteAddr;
    private Instant lastLoggedIn;
}
