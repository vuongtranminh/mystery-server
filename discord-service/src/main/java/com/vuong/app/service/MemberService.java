package com.vuong.app.service;

import com.vuong.app.doman.*;
import com.vuong.app.dto.member.MemberFilterParameter;
import com.vuong.app.dto.member.MemberOptions;
import com.vuong.app.dto.member.MemberSortParameter;
import com.vuong.app.jpa.query.QueryBuilder;
import com.vuong.app.jpa.query.QueryHelper;
import com.vuong.app.operator.ListOperators;
import com.vuong.app.operator.NumberOperators;
import com.vuong.app.operator.SortOrder;
import com.vuong.app.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.Fetch;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import java.util.Arrays;
import java.util.Optional;

@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;

    public Page<MemberRepository.MemberWithServer> getMembersByProfileId(Integer profileId, int page, int size) {
        NumberOperators profileIdOperators = new NumberOperators();
        profileIdOperators.setEq(Float.valueOf(profileId));

        MemberFilterParameter memberFilterParameter = new MemberFilterParameter();
        memberFilterParameter.setProfileId(profileIdOperators);

        MemberSortParameter memberSortParameter = new MemberSortParameter();
        memberSortParameter.setUpdatedAt(SortOrder.ASC);

        MemberOptions memberOptions = new MemberOptions();
        memberOptions.setFilter(memberFilterParameter);
        memberOptions.setSort(memberSortParameter);

        QueryBuilder queryBuilder = new QueryBuilder();

        queryBuilder.query(this.fetchServer());
        this.buildFilter(queryBuilder, memberFilterParameter);
        this.buildSortOrder(queryBuilder, memberSortParameter);

        Page<MemberRepository.MemberWithServer> pageMember = this.memberRepository.findAll(queryBuilder.build(), MemberRepository.MemberWithServer.class, PageRequest.of(page, size));
        return pageMember;
    }

    public Optional<MemberRepository.MemberWithServer> getMemberWithPermission(Integer profileId, Integer serverId) {
        NumberOperators profileIdOperators = new NumberOperators();
        profileIdOperators.setEq(Float.valueOf(profileId));

        NumberOperators serverIdOperators = new NumberOperators();
        serverIdOperators.setEq(Float.valueOf(serverId));

        ListOperators<String> memberRoles = new ListOperators<>();
        memberRoles.setIn(Arrays.asList(MemberRole.ADMIN.name(), MemberRole.MODERATOR.name()));

        MemberFilterParameter memberFilterParameter = new MemberFilterParameter();
        memberFilterParameter.setProfileId(profileIdOperators);
        memberFilterParameter.setServerId(serverIdOperators);
        memberFilterParameter.setMemberRoles(memberRoles);

        QueryBuilder queryBuilder = new QueryBuilder();

        queryBuilder.query(this.fetchServer());
        this.buildFilter(queryBuilder, memberFilterParameter);

        Optional<MemberRepository.MemberWithServer> member = this.memberRepository.findOne(queryBuilder.build(), MemberRepository.MemberWithServer.class);
        return member;
    }

    private void buildSortOrder(QueryBuilder queryBuilder, MemberSortParameter sortParameter) {
        if (sortParameter == null) return ;
        QueryHelper.buildOneSortOrder(queryBuilder, sortParameter.getCreatedAt(), Member_.CREATED_AT);
        QueryHelper.buildOneSortOrder(queryBuilder, sortParameter.getUpdatedAt(), Member_.UPDATED_AT);
    }

    private void buildFilter(QueryBuilder queryBuilder, MemberFilterParameter filterParameter) {
        if (filterParameter == null) return;
        QueryHelper.buildOneNumberOperatorFilter(queryBuilder, filterParameter.getMemberId(), Member_.MEMBER_ID);
        QueryHelper.buildOneNumberOperatorFilter(queryBuilder, filterParameter.getProfileId(), Member_.PROFILE_ID);
        QueryHelper.buildOneNumberOperatorFilter(queryBuilder, filterParameter.getServerId(), Server_.SERVER_ID);
        QueryHelper.buildOneListOperatorFilter(queryBuilder, filterParameter.getMemberRoles(), Member_.MEMBER_ROLE);
        QueryHelper.buildOneDateOperatorFilter(queryBuilder, filterParameter.getCreatedAt(), Member_.CREATED_AT);
        QueryHelper.buildOneDateOperatorFilter(queryBuilder, filterParameter.getUpdatedAt(), Member_.UPDATED_AT);
    }

    private Specification<Member> fetchServer() {
        return (root, query, criteriaBuilder) -> {
//            Fetch<Member, Server> f = root.fetch(Member_.SERVER, JoinType.INNER);
//            Join<Member, Server> join = (Join<Member, Server>) f;
            Fetch<Object, Object> f = root.fetch(Member_.SERVER, JoinType.INNER);
            Join<Object, Object> join = (Join<Object, Object>) f;
            return join.getOn();
        };
    }

}
