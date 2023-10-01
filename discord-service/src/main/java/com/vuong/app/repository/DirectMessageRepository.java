package com.vuong.app.repository;

import com.vuong.app.doman.DirectMessage;
import com.vuong.app.jpa.specification.JpaSpecificationExecutorWithProjection;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DirectMessageRepository extends JpaRepository<DirectMessage, Integer>, JpaSpecificationExecutorWithProjection<DirectMessage, Integer> {
}
