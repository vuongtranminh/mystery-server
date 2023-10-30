package com.vuong.app.dto.member;

import com.vuong.app.operator.*;
import lombok.Data;

@Data
public class MemberFilterParameter {
    private NumberOperators memberId;
    private NumberOperators profileId;
    private NumberOperators serverId;
    private ListOperators memberRoles;

    private DateOperators createdAt;
    private DateOperators updatedAt;
}
