package com.vuong.app.business.user.model;

import com.vuong.app.operator.StringOperators;
import lombok.Data;

@Data
public class UserFilterParameter {
    private StringOperators name;
    private StringOperators avatar;
    private StringOperators bio;
}
