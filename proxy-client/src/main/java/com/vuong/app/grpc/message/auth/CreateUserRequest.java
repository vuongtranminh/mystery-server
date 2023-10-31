package com.vuong.app.grpc.message.auth;

import com.vuong.app.business.auth.model.AuthProvider;
import com.vuong.app.common.Node;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class CreateUserRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    private String name;
    private String avtUrl;
    private String email;
    private String password;
    private boolean emailVerified;
    private AuthProvider provider;
    private String providerId;

    public String getName() {
        return name != null ? name : "";
    }

    public String getAvatar() {
        return avtUrl != null ? avtUrl : "";
    }

    public String getEmail() {
        return email != null ? email : "";
    }

    public String getPassword() {
        return password != null ? password : "";
    }

    public AuthProvider getProvider() {
        return provider;
    }

    public String getProviderId() {
        return providerId != null ? providerId : "";
    }
}
