package com.vuong.app.doman;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.io.Serializable;
import java.time.Instant;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public final class Message implements Serializable {
    private static final long serialVersionUID = 1L;

    private String messageId;

    private String content;

    private String fileUrl;

    private String createdBy;

    private String channelId;

    private String deletedAt;

    private String createdAt;

    private String updatedAt;

}
