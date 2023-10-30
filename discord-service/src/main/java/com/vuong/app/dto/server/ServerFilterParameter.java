package com.vuong.app.dto.server;

import com.vuong.app.operator.DateOperators;
import com.vuong.app.operator.ListOperators;
import com.vuong.app.operator.NumberOperators;
import com.vuong.app.operator.StringOperators;
import lombok.Data;

@Data
public class ServerFilterParameter {
    private NumberOperators serverId;
    private StringOperators name;
    private StringOperators inviteCode;
    private NumberOperators profileId;

    private DateOperators createdAt;
    private DateOperators updatedAt;
}
