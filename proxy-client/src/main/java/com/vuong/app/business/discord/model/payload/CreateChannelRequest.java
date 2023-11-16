package com.vuong.app.business.discord.model.payload;

import com.vuong.app.business.discord.model.ChannelType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class CreateChannelRequest {
    private String serverId;
    private String name;
    private int type;
}
