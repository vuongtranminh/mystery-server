package com.vuong.app.repository;

import com.vuong.app.doman.Channel;
import com.vuong.app.jpa.specification.JpaSpecificationExecutorWithProjection;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChannelRepository extends JpaRepository<Channel, Integer>, JpaSpecificationExecutorWithProjection<Channel, Integer> {
}
