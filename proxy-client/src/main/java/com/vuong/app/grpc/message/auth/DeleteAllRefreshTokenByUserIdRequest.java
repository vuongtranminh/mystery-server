package com.vuong.app.grpc.message.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class DeleteAllRefreshTokenByUserIdRequest {
    private Integer userId;
}
