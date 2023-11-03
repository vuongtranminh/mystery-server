package com.vuong.app.doman;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;

import java.io.Serializable;
import java.util.Set;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public final class Server extends AbstractMappedEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer serverId;

    private String name;

    private String imgUrl;

    private String inviteCode;

    private Integer profileId;

    private Set<Member> members;

    private Set<Channel> channels;

    public void addMember(Member member) {
        members.add(member);
        member.setServer(this);
    }

    public void removeMember(Member member) {
        members.remove(member);
        member.setServer(null);
    }

    public void addChanel(Channel channel) {
        channels.add(channel);
        channel.setServer(this);
    }

    public void removeMember(Channel channel) {
        channels.remove(channel);
        channel.setServer(null);
    }
}
