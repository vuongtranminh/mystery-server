package com.vuong.app.doman;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class Message {
    private String messageId;
    private String content;
    private String fileUrl;
    private String channelId;
    private String createdAt;
    private String updatedAt;
    private String deletedAt;
    private String deletedBy;
    private MemberProfile author;
}
