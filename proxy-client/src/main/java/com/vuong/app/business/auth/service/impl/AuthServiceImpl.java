package com.vuong.app.business.auth.service.impl;

import com.vuong.app.business.auth.model.AuthProvider;
import com.vuong.app.business.auth.model.payload.SignUpRequest;
import com.vuong.app.business.auth.service.AuthService;
import com.vuong.app.grpc.message.auth.CreateUserRequest;
import com.vuong.app.grpc.message.auth.CreateUserResponse;
import com.vuong.app.grpc.service.AuthClientService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthClientService authClientService;

    @Override
    public Integer create(SignUpRequest request) {
        Optional<CreateUserResponse> createUserResponseOptional = this.authClientService.create(CreateUserRequest.builder()
                .name(request.getName())
                .email(request.getEmail())
                .provider(AuthProvider.local)
                .build());

        if (!createUserResponseOptional.isPresent()) {
            throw new RuntimeException("Create error!");
        }

        return createUserResponseOptional.get().getUserId();
    }

    @Override
    public boolean existsByEmail(String email) {
        return this.authClientService.existsByEmail(email);
    }
}
