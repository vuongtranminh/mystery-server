package com.vuong.app.service;

import com.vuong.app.doman.Member;
import com.vuong.app.doman.Member_;
import com.vuong.app.doman.Server;
import com.vuong.app.doman.Server_;
import com.vuong.app.dto.member.MemberFilterParameter;
import com.vuong.app.dto.member.MemberSortParameter;
import com.vuong.app.dto.server.ServerFilterParameter;
import com.vuong.app.dto.server.ServerSortParameter;
import com.vuong.app.jpa.query.QueryBuilder;
import com.vuong.app.jpa.query.QueryHelper;
import com.vuong.app.repository.DiscordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.Fetch;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;

@RequiredArgsConstructor
public class DiscordService {

    private final DiscordRepository discordRepository;

    public boolean leaveServer(Integer serverId, Integer profileId) {
        return this.discordRepository.leaveServer(serverId, profileId);
    }



//    private void buildSortOrder(QueryBuilder queryBuilder, MemberSortParameter memberSortParameter, ServerSortParameter serverSortParameter) {
//        if (memberSortParameter != null) {
//            QueryHelper.buildOneSortOrder(queryBuilder, memberSortParameter.getCreatedAt(), Member_.CREATED_AT);
//            QueryHelper.buildOneSortOrder(queryBuilder, memberSortParameter.getUpdatedAt(), Member_.UPDATED_AT);
//        }
//
//        if (serverSortParameter != null) {
//            QueryHelper.buildOneSortOrder(queryBuilder, serverSortParameter.getCreatedAt(), Server_.CREATED_AT);
//            QueryHelper.buildOneSortOrder(queryBuilder, serverSortParameter.getUpdatedAt(), Server_.UPDATED_AT);
//        }
//    }
//
//    private void buildFilter(QueryBuilder queryBuilder, MemberFilterParameter memberFilterParameter, ServerFilterParameter serverFilterParameter) {
//        if (memberFilterParameter != null) {
//            QueryHelper.buildOneNumberOperatorFilter(queryBuilder, memberFilterParameter.getMemberId(), Member_.MEMBER_ID);
//            QueryHelper.buildOneNumberOperatorFilter(queryBuilder, memberFilterParameter.getProfileId(), Member_.PROFILE_ID);
//            QueryHelper.buildOneNumberOperatorFilter(queryBuilder, memberFilterParameter.getServerId(), Server_.SERVER_ID);
//            QueryHelper.buildOneListOperatorFilter(queryBuilder, memberFilterParameter.getMemberRoles(), Member_.MEMBER_ROLE);
//            QueryHelper.buildOneDateOperatorFilter(queryBuilder, memberFilterParameter.getCreatedAt(), Member_.CREATED_AT);
//            QueryHelper.buildOneDateOperatorFilter(queryBuilder, memberFilterParameter.getUpdatedAt(), Member_.UPDATED_AT);
//        }
//
//        if (serverFilterParameter != null) {
//            QueryHelper.buildOneNumberOperatorFilter(queryBuilder, serverFilterParameter.getServerId(), Server_.SERVER_ID);
//            QueryHelper.buildOneStringOperatorFilter(queryBuilder, serverFilterParameter.getName(), Server_.NAME);
//            QueryHelper.buildOneStringOperatorFilter(queryBuilder, serverFilterParameter.getInviteCode(), Server_.INVITE_CODE);
//            QueryHelper.buildOneNumberOperatorFilter(queryBuilder, serverFilterParameter.getProfileId(), Server_.PROFILE_ID);
//            QueryHelper.buildOneDateOperatorFilter(queryBuilder, serverFilterParameter.getCreatedAt(), Server_.CREATED_AT);
//            QueryHelper.buildOneDateOperatorFilter(queryBuilder, serverFilterParameter.getUpdatedAt(), Server_.UPDATED_AT);
//        }
//    }
//
//    private Specification<Member> fetchServer() {
//        return (root, query, criteriaBuilder) -> {
//            Fetch<Member, Server> f = root.fetch(Member_.SERVER, JoinType.INNER);
//            Join<Member, Server> join = (Join<Member, Server>) f;
//            return join.getOn();
//        };
//    }
}
