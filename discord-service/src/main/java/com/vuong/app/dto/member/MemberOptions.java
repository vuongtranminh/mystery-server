package com.vuong.app.dto.member;

import lombok.Data;

@Data
public class MemberOptions {
    private MemberSortParameter sort;
    private MemberFilterParameter filter;
}
