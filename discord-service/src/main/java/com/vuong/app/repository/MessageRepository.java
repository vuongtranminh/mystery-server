package com.vuong.app.repository;

import com.vuong.app.doman.Message;
import com.vuong.app.jpa.specification.JpaSpecificationExecutorWithProjection;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MessageRepository extends JpaRepository<Message, Integer>, JpaSpecificationExecutorWithProjection<Message, Integer> {
}
