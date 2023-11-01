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
public class CreateUserSocialRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    private String name;
    private String avtUrl;
    private String email;
    private AuthProvider provider;
    private String providerId;

}
