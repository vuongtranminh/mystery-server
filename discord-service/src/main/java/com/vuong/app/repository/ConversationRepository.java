package com.vuong.app.repository;

import com.vuong.app.doman.Conversation;
import com.vuong.app.jpa.specification.JpaSpecificationExecutorWithProjection;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConversationRepository extends JpaRepository<Conversation, Integer>, JpaSpecificationExecutorWithProjection<Conversation, Integer> {
}
