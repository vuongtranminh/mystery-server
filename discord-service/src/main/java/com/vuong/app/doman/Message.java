package com.vuong.app.doman;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.*;
import java.io.Serializable;
import java.time.Instant;

@Entity
@Table(name = "messages")
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true, exclude = {"member", "channel"})
@ToString(exclude = {"member", "channel"})
@Data
@Builder
@DynamicInsert
@DynamicUpdate
public final class Message extends AbstractMappedEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @PrePersist
    public void prePersist() {
        this.deleted = this.deleted != null ? this.deleted : Boolean.FALSE;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "message_id", unique = true, nullable = false, updatable = false)
    private Integer messageId;

    @Column(name = "content")
    private String content;

    @Column(name = "file_url")
    private String fileUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "channel_id")
    private Channel channel;

    @Column(name = "deleted")
    private Boolean deleted;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @Column(name = "deleted_at")
    private Instant deletedAt;

}
