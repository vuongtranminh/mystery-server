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
public class UpdateUserByUserIdRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer userId;
    private String name;
    private String avatar;
    private String bio;

}
