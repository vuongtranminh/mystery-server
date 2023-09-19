package com.vuong.app.business.auth.service;

import com.vuong.app.business.auth.model.payload.SignUpRequest;

public interface AuthService {

    Integer create(SignUpRequest request);
    boolean existsByEmail(String email);
}
