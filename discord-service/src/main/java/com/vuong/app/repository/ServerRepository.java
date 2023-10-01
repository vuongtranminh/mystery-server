package com.vuong.app.repository;

import com.vuong.app.doman.Server;
import com.vuong.app.jpa.specification.JpaSpecificationExecutorWithProjection;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ServerRepository extends JpaRepository<Server, Integer>, JpaSpecificationExecutorWithProjection<Server, Integer> {
    Optional<Server> findByInviteCode(String inviteCode);
}
