package com.vuong.app.business;

import com.vuong.app.v1.GrpcMeta;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class Meta {
    private long totalElements;
    private int totalPages;
    private int pageNumber;
    private int pageSize;

    public static Meta parse(GrpcMeta grpcMeta) {
        return Meta.builder()
                .totalElements(grpcMeta.getTotalElements())
                .totalPages(grpcMeta.getTotalPages())
                .pageNumber(grpcMeta.getPageNumber())
                .pageSize(grpcMeta.getPageSize())
                .build();
    }
}
