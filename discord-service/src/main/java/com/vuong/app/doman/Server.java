package com.vuong.app.doman;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Set;

// You can have two indexes of the same name. They just can't be in the same schema. Just like you can have two tables of the same name, but not in the same schema.
@Entity
@Table(name = "servers", indexes = {
        @Index(name = "idx_servers_invite_code", columnList = "invite_code"),
        @Index(name = "idx_servers_profile_id", columnList = "profile_id")
})
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true, exclude = {"members", "channels"})
@ToString(exclude = {"members", "channels"})
@Data
@Builder
@DynamicInsert
@DynamicUpdate
public final class Server extends AbstractMappedEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "server_id", unique = true, nullable = false, updatable = false)
    private Integer serverId;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "img_url")
    private String imgUrl;

    @Column(name = "invite_code", unique = true, nullable = false)
    private String inviteCode;

    @Column(name = "profile_id", nullable = false)
    private Integer profileId;

    @JsonIgnore
    @OneToMany(mappedBy = "server", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Member> members;

    @JsonIgnore
    @OneToMany(mappedBy = "server", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
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
