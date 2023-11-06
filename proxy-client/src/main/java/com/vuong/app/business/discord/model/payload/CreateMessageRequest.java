package com.vuong.app.business.discord.model.payload;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class CreateMessageRequest {
    private String content;
    private String fileUrl;
    private String channelId;
    private String serverId;
}
