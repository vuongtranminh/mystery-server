package com.vuong.app.business.auth.controller;

import com.vuong.app.business.auth.model.payload.*;
import com.vuong.app.business.auth.service.AuthService;
import com.vuong.app.common.api.ResponseMsg;
import com.vuong.app.config.AppProperties;
import com.vuong.app.security.CurrentUser;
import com.vuong.app.security.TokenProvider;
import com.vuong.app.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.net.URI;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signin")
    public ResponseEntity<?> authenticateUser(HttpServletRequest request, HttpServletResponse response, @Valid @RequestBody SignInRequest signInRequest) {
        return ResponseEntity.ok(this.authService.signIn(request, response, signInRequest));
    }

    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignUpRequest signUpRequest) {
        ResponseMsg responseMsg = (ResponseMsg) this.authService.signUp(signUpRequest);
        SignUpResponse signUpResponse = (SignUpResponse) responseMsg.getData();

        // Creating user's account

        URI location = ServletUriComponentsBuilder
                .fromCurrentContextPath().path("/user/me")
                .buildAndExpand(signUpResponse.getUserId()).toUri();

        return ResponseEntity.created(location)
                .body(responseMsg);
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request, HttpServletResponse response, @CurrentUser UserPrincipal currentUser) {
        return ResponseEntity.ok(this.authService.logout(request, response, currentUser));
    }

    @PostMapping("/refeshToken")
    public ResponseEntity<?> refeshToken(HttpServletRequest request, HttpServletResponse response) {
        return ResponseEntity.ok(this.authService.refeshToken(request, response));
    }

    @GetMapping("/accountVerificationToken/{verificationToken}")
    public ResponseEntity<?> verifyAccount(@PathVariable String verificationToken) {
        return ResponseEntity.ok(this.authService.verificationCredential(false, verificationToken));
    }

    @GetMapping("/accountVerificationOtp")
    public ResponseEntity<?> verifyAccount(
            @RequestBody
            @NotNull(message = "*Input must not blank!**")
            @Valid final VerificationCredentialRequest request
    ) {
        return ResponseEntity.ok(this.authService.verificationCredential(true, request.getVerificationOtp()));
    }

    @PostMapping("/reissueVerificationCredential")
    public ResponseEntity<?> reissueVerificationCredential(
            @RequestBody
            @NotNull(message = "*Input must not blank!**")
            @Valid final ReissueVerificationCredentialRequest request
    ) {
        return ResponseEntity.ok(this.authService.reissueVerificationCredential(request));
    }

    @PostMapping("/changeUserPassword")
    public ResponseEntity<?> changeUserPassword(
            @RequestBody
            @NotNull(message = "*Input must not blank!**")
            @Valid final ChangeUserPasswordRequest request
    ) {
        return ResponseEntity.ok(this.authService.changeUserPassword(request));
    }
}
