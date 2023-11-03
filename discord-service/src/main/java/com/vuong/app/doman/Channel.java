package com.vuong.app.doman;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import java.io.Serializable;
import java.util.Set;

@Data
@Builder
public final class Channel extends AbstractMappedEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer channelId;

    private String name;

    private ChannelType type;

    private Integer profileId;

    private Server server;

    private Set<Message> messages;

    public void addMessage(Message message) {
        messages.add(message);
        message.setChannel(this);
    }

    public void removeMessage(Message message) {
        messages.remove(message);
        message.setChannel(null);
    }

}
