package com.vuong.app.grpc.message.discord;

import com.vuong.app.grpc.message.MessageMeta;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class MessageGetServersJoinResponse {
    MessageMeta meta;
    List<MessageServer> content;
}
