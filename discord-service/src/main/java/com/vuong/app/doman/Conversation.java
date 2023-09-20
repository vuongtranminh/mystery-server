package com.vuong.app.doman;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Set;

@Entity
@Table(name = "conversation", uniqueConstraints = @UniqueConstraint(columnNames = {"member_one_id", "member_two_id"}))
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true, exclude = {"memberOne", "memberTwo", "directMessages"})
@ToString(exclude = {"memberOne", "memberTwo", "directMessages"})
@Data
@Builder
@DynamicInsert
@DynamicUpdate
public final class Conversation extends AbstractMappedEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "conversation_id", unique = true, nullable = false, updatable = false)
    private Integer conversationId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_one_id")
    private Member memberOne;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_two_id")
    private Member memberTwo;

    @JsonIgnore
    @OneToMany(mappedBy = "conversation", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
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
