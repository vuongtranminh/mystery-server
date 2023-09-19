package com.vuong.app.business.auth.model.payload;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import java.io.Serializable;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class LoginRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    @NotBlank
    @Email
    private String email;

    @NotBlank
    private String password;
}
