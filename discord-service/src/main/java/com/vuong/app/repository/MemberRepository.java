package com.vuong.app.repository;

import com.vuong.app.doman.Member;
import com.vuong.app.doman.Server;
import com.vuong.app.jpa.specification.JpaSpecificationExecutorWithProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Integer>, JpaSpecificationExecutorWithProjection<Member, Integer> {

    interface MemberWithServer {
        Server getServer();
    }

    Optional<Member> findByProfileIdAndAndServer(Integer profileId, Server server);
}
