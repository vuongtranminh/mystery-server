package com.vuong.app.grpc.message.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class ChangeUserPasswordByUserIdRequest {
    private String userId;
    private String oldPassword;
    private String newPassword;
}
