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
    private Server result;
}
