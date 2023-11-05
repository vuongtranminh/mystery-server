package com.vuong.app.grpc.message.discord;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class MessageMessage {
    private String messageId;
    private String content;
    private String fileUrl;
    private String channelId;
    private String createdAt;
    private String updatedAt;
    private String deletedAt;
    private String deletedBy;
    private MessageMemberProfile author;
}
