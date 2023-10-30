package com.vuong.app.repository;

import com.vuong.app.doman.Member;

import java.util.Optional;

public interface DiscordRepository {
    boolean leaveServer(Integer serverId, Integer profileId);
}
