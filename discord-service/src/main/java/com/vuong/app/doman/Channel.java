package com.vuong.app.doman;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Set;

@Entity
@Table(name = "channels", indexes = {
        @Index(name = "idx_channels_profile_id", columnList = "profile_id")
})
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true, exclude = {"server", "messages"})
@ToString(exclude = {"server", "messages"})
@Data
@Builder
@DynamicInsert
@DynamicUpdate
public final class Channel extends AbstractMappedEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "channel_id", unique = true, nullable = false, updatable = false)
    private Integer channelId;

    @Column(name = "name")
    private String name;

    @Column(name = "type", nullable = false)
    @Enumerated(EnumType.STRING)
    private ChannelType type;

    @Column(name = "profile_id", nullable = false)
    private Integer profileId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "server_id")
    private Server server;

    @JsonIgnore
    @OneToMany(mappedBy = "channel", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
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
