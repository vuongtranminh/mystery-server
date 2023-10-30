package com.vuong.app.grpc.message.discord;

import lombok.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class MessageGetServersByProfileIdResponse {

    @NoArgsConstructor
    @AllArgsConstructor
    @Data
    @Builder
    public static class MessageServerItem {
        private Integer serverId;
        private String name;
        private String imgUrl;
        private String inviteCode;
        private Integer profileId;
        private Instant createdAt;
        private Instant updatedAt;
    }

    private List<MessageServerItem> items = new ArrayList<>();
    private int totalItems;
}
