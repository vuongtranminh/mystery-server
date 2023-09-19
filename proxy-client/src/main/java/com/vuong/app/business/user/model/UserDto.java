package com.vuong.app.business.user.model;

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
public class UserDto implements Serializable {
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
