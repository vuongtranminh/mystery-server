package com.vuong.app.grpc.message.auth;

import com.vuong.app.business.auth.model.AuthProvider;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class GetUserByEmailResponse implements UserDto, Serializable {
    private static final long serialVersionUID = 1L;

    private Integer userId;
    private String name;
    private String avatar;
    private String bio;
    private String email;
    private String password;
    private Boolean verified;
    private AuthProvider provider;
    private String providerId;
}
