package com.vuong.app.business.discord.model.payload;

import com.vuong.app.business.Meta;
import com.vuong.app.business.discord.model.Message;
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
public class GetMessagesByChannelIdResponse {
    private Meta meta;
    private List<Message> content;
}
