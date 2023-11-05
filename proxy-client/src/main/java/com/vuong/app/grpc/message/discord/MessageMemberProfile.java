package com.vuong.app.grpc.message.discord;

import com.vuong.app.business.discord.model.MemberRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class MessageMemberProfile {
    private String memberId;
    private MemberRole role;
    private String serverId;
    private String joinAt;
    private String profileId;
    private String name;
    private String avtUrl;
}
