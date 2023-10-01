package com.vuong.app.repository;

import com.vuong.app.doman.RefreshToken;
import com.vuong.app.doman.User;
import com.vuong.app.jpa.specification.JpaSpecificationExecutorWithProjection;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Integer>, JpaSpecificationExecutorWithProjection<RefreshToken, Integer> {
    Optional<RefreshToken> findByRefreshToken(String refreshToken);

    void deleteByRefreshToken(String refreshToken);

    void deleteAllByUser(User user);

    boolean existsByRefreshToken(String refreshToken);
}
