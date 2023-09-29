package com.vuong.app.repository;

import com.vuong.app.doman.User;
import com.vuong.app.doman.VerificationCredential;
import com.vuong.app.jpa.specification.JpaSpecificationExecutorWithProjection;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VerificationCredentialRepository extends JpaRepository<VerificationCredential, Integer>, JpaSpecificationExecutorWithProjection<VerificationCredential, Integer> {
    Optional<VerificationCredential> findByVerificationToken(String verificationToken);
    Optional<VerificationCredential> findByVerificationOtp(String verificationOtp);
}
