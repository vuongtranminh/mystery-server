package com.vuong.app.doman;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.io.Serializable;
import java.time.Instant;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public final class Message extends AbstractMappedEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer messageId;

    private String content;

    private String fileUrl;

    private Member member;

    private Channel channel;

    private Boolean deleted;

    private Instant deletedAt;

}
