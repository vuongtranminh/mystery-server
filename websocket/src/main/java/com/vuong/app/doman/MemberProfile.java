package com.vuong.app.doman;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class MemberProfile {
    private String memberId;
    private Integer role;
    private String serverId;
    private String joinAt;
    private String profileId;
    private String name;
    private String avtUrl;
}
