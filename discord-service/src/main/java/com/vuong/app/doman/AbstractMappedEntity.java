package com.vuong.app.doman;

import lombok.*;
import java.io.Serializable;
import java.time.Instant;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Data
@Builder
abstract public class AbstractMappedEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    private Instant createdAt;

    private Instant updatedAt;

}