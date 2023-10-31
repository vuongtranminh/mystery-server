package com.vuong.app.doman;

import lombok.*;
import java.io.Serializable;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public final class User extends AbstractMappedEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    private String userId;
    private String name;
    private String avt_url;
    private String bio;
    private String email;
    private String password;
    private Boolean verified;
    private AuthProvider provider;
    private String providerId;
}
