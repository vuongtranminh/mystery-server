package com.vuong.app.grpc.message.discord;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class MessageGetServerByServerIdResponse {
    private Integer serverId;
    private String name;
    private String imgUrl;
    private String inviteCode;
    private Integer profileId;
    private Instant createdAt;
    private Instant updatedAt;
}
