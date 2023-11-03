package com.vuong.app.doman;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;

import java.io.Serializable;
import java.util.Set;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public final class Member extends AbstractMappedEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer memberId;

    private MemberRole memberRole;

    private Integer profileId;

    private Server server;

    private Set<Message> messages;

    private Set<DirectMessage> directMessages;

    private Set<Conversation> conversationsInitiated;

    private Set<Conversation> conversationsReceived;

}
