package com.vuong.app.grpc.message.auth;

import com.vuong.app.business.auth.model.AuthProvider;
import com.vuong.app.business.auth.model.UserDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class GetUserByUserIdResponse implements UserDto, Serializable {
    private static final long serialVersionUID = 1L;

    private Integer userId;
    private String name;
    private String avatar;
    private String bio;
    private String email;
    private String password;
    private AuthProvider provider;
    private String providerId;
}