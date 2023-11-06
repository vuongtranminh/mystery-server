package com.vuong.app.business.discord.model.payload;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class GetMessagesByChannelIdRequest {
    private String channelId;
    private String serverId;
    private int pageNumber;
    private int pageSize;
}
