package com.vuong.app.repository;

import com.vuong.app.doman.AuthProvider;
import com.vuong.app.doman.RefreshToken;
import com.vuong.app.doman.User;
import com.vuong.app.jpa.specification.JpaSpecificationExecutorWithProjection;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.Optional;
import java.util.Set;

public interface UserRepository extends JpaRepository<User, Integer>, JpaSpecificationExecutorWithProjection<User, Integer> {

    interface UserWithoutVerificationCredential{
        Integer getUserId();
        String getName();
        String getAvatar();
        String getBio();
        String getEmail();
        String getPassword();
        Boolean getVerified();
        AuthProvider getProvider();
        String getProviderId();
        Instant getCreatedAt();
        Instant getUpdatedAt();
    }

    interface UserVerified{
        Boolean getVerified();
    }

    boolean existsByEmail(String email);
    Optional<User> findByEmail(String email);
}
