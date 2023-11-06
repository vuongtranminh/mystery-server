package com.vuong.app.business.discord.model.payload;

import com.vuong.app.business.discord.model.MemberProfile;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class GetMemberByServerIdResponse {
    private MemberProfile result;
}
