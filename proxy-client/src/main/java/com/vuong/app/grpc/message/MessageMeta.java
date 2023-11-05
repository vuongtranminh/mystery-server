package com.vuong.app.grpc.message;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class MessageMeta {
    private long totalElements;
    private int totalPages;
    private int pageNumber;
    private int pageSize;
}
