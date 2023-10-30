package com.vuong.app.dto.member;

import com.vuong.app.operator.SortOrder;
import lombok.Data;

@Data
public class MemberSortParameter {
    private SortOrder createdAt;
    private SortOrder updatedAt;
}
