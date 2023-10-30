package com.vuong.app.dto.server;

import com.vuong.app.operator.SortOrder;
import lombok.Data;

@Data
public class ServerSortParameter {
    private SortOrder createdAt;
    private SortOrder updatedAt;
}
