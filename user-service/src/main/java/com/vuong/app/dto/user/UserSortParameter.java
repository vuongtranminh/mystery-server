package com.vuong.app.dto.user;

import com.vuong.app.operator.SortOrder;
import lombok.Data;

@Data
public class UserSortParameter {
    private SortOrder createdAt;
    private SortOrder updatedAt;
}
