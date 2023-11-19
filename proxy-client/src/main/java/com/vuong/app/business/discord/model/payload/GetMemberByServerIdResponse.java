package com.vuong.app.business.discord.model.payload;

import com.vuong.app.business.discord.model.MemberProfile;
import com.vuong.app.business.discord.model.MemberRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class GetMemberByServerIdResponse {
    private String memberId;
    private Integer role;
    private String serverId;
    private String joinAt;
    private String profileId;
    private String name;
    private String avtUrl;
}
