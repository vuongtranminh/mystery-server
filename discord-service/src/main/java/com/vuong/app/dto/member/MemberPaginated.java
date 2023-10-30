package com.vuong.app.dto.member;

import com.vuong.app.doman.Member;
import com.vuong.app.dto.PaginatedList;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class MemberPaginated implements PaginatedList<Member> {
    private List<Member> items = new ArrayList<>();
    private Integer totalItems;
}
