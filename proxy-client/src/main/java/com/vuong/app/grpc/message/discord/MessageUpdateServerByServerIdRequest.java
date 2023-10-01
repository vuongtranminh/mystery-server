package com.vuong.app.grpc.message.discord;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class MessageUpdateServerByServerIdRequest {
    private Integer profileId;
    private Integer serverId;
    private String name;
    private String imgUrl;
}
