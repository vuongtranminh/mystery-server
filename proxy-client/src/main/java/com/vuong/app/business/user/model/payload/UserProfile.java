package com.vuong.app.business.user.model.payload;

import com.vuong.app.business.auth.model.AuthProvider;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class UserProfile {
    private Integer userId;
    private String name;
    private String avatar;
    private String bio;
    private String email;
    private AuthProvider provider;
}
