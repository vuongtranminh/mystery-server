package com.vuong.app.grpc.message.discord;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class MessageServer {
    private String serverId;
    private String name;
    private String imgUrl;
    private String authorId;
    private String createdAt;
    private String updatedAt;
}
