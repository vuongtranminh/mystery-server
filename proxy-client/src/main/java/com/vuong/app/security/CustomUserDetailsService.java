package com.vuong.app.security;

import com.vuong.app.grpc.message.auth.GetUserByEmailRequest;
import com.vuong.app.grpc.message.auth.GetUserByUserIdRequest;
import com.vuong.app.grpc.message.auth.UserDto;
import com.vuong.app.grpc.service.AuthClientService;
import com.vuong.app.exception.wrapper.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final AuthClientService authClientService;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        UserDto userDto = this.authClientService.getUserByEmail(GetUserByEmailRequest.builder().email(email).build())
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email : " + email));

        return UserPrincipal.create(userDto);
    }

    public UserDetails loadUserById(String userId) {
        UserDto userDto = this.authClientService.getUserByUserId(GetUserByUserIdRequest.builder().userId(userId).build())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        return UserPrincipal.create(userDto);
    }
}
