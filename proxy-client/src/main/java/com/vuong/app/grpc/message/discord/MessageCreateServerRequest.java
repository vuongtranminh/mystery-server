package com.vuong.app.grpc.message.discord;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class MessageCreateServerRequest {
    private String name;
    private String imgUrl;
    private Integer profileId;
}
