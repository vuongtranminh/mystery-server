package com.vuong.app.business.discord.model.payload;

import com.vuong.app.business.discord.model.Server;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class GetFirstServerJoinResponse {
    private String serverId;
    private String name;
    private String imgUrl;
    private String inviteCode;
    private String authorId;
    private String createdAt;
    private String updatedAt;
}
