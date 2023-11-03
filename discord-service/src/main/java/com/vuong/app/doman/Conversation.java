package com.vuong.app.doman;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;

import java.io.Serializable;
import java.util.Set;

@Data
@Builder
public final class Conversation extends AbstractMappedEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer conversationId;

    private Member memberOne;

    private Member memberTwo;

    private Set<DirectMessage> directMessages;

    public void addDirectMessage(DirectMessage directMessage) {
        directMessages.add(directMessage);
        directMessage.setConversation(this);
    }

    public void removeDirectMessage(DirectMessage directMessage) {
        directMessages.remove(directMessage);
        directMessage.setConversation(null);
    }
}
