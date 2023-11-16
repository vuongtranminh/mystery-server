package com.vuong.app.business.discord.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class Channel {
    private String channelId;
    private String name;
    private int type;
    private String serverId;
    private String createdAt;
    private String updatedAt;
}
