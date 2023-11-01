package com.vuong.app.grpc.message.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class GetUserPrincipalByUserIdRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    private String userId;
}
