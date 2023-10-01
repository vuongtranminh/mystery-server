package com.vuong.app.business.auth.model.payload;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class ChangeUserPasswordRequest {
    private Integer userId;
    private String oldPassword;
    private String newPassword;
}
