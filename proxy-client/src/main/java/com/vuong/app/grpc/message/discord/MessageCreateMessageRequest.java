package com.vuong.app.grpc.message.discord;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class MessageCreateMessageRequest {
    private String content;
    private String fileUrl;
    private String channelId;
    private String serverId;
    private String profileId;
}
