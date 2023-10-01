package com.vuong.app.repository;

import com.vuong.app.doman.Member;
import com.vuong.app.jpa.specification.JpaSpecificationExecutorWithProjection;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Member, Integer>, JpaSpecificationExecutorWithProjection<Member, Integer> {
}
