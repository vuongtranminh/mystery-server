package com.vuong.app.repository;

import com.vuong.app.doman.User;
import com.vuong.app.jpa.specification.JpaSpecificationExecutorWithProjection;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Integer>, JpaSpecificationExecutorWithProjection<User, Integer> {
    static interface UserTest{
        String getName();
        String getBio();
    }

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);
}
