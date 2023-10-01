package com.vuong.app.dto.user;

import com.vuong.app.operator.BooleanOperators;
import com.vuong.app.operator.DateOperators;
import com.vuong.app.operator.NumberOperators;
import com.vuong.app.operator.StringOperators;
import lombok.Data;

@Data
public class UserFilterParameter {
    private NumberOperators userId;
    private StringOperators name;
    private StringOperators email;
    private BooleanOperators verified;

    private DateOperators createdAt;
    private DateOperators updatedAt;
}
