package com.vuong.app.doman;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;
import java.io.Serializable;
import java.time.Instant;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public final class DirectMessage extends AbstractMappedEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer directMessageId;

    private String content;

    private String fileUrl;

    private Member member;

    private Conversation conversation;

    private Boolean deleted;

    private Instant deletedAt;
}
