package com.vuong.app.security;

import com.vuong.app.grpc.message.auth.*;
import com.vuong.app.grpc.service.AuthClientService;
import com.vuong.app.exception.wrapper.ResourceNotFoundException;
import com.vuong.app.redis.repository.UserRepository;
import com.vuong.app.v1.auth.GrpcUserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final AuthClientService authClientService;
    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        GetUserPrincipalByEmailResponse userPrincipal = this.authClientService.getUserPrincipalByEmail(GetUserPrincipalByEmailRequest.builder().email(email).build())
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email : " + email));

        return UserPrincipal.create(userPrincipal);
    }

    public UserDetails loadUserById(String userId) {

        GetUserPrincipalByUserIdResponse userPrincipal = null;

        GrpcUserPrincipal grpcUserPrincipal = userRepository.getUserByUserId(userId);

        if (grpcUserPrincipal == null) {
            userPrincipal = this.authClientService.getUserPrincipalByUserId(GetUserPrincipalByUserIdRequest.builder().userId(userId).build())
                    .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        } else {
            userPrincipal = GetUserPrincipalByUserIdResponse.builder()
                    .userId(grpcUserPrincipal.getUserId())
                    .email(grpcUserPrincipal.getEmail())
                    .password(grpcUserPrincipal.getPassword())
                    .build();
        }

        return UserPrincipal.create(userPrincipal);
    }
}
