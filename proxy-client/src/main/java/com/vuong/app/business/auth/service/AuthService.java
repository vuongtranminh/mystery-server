package com.vuong.app.business.auth.service;

import com.vuong.app.business.auth.model.payload.ChangeUserPasswordRequest;
import com.vuong.app.business.auth.model.payload.ReissueVerificationCredentialRequest;
import com.vuong.app.business.auth.model.payload.SignInRequest;
import com.vuong.app.business.auth.model.payload.SignUpRequest;
import com.vuong.app.common.api.ResponseObject;
import com.vuong.app.security.UserPrincipal;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface AuthService {

//    boolean existsByEmail(String email);

    ResponseObject signIn(HttpServletRequest request, HttpServletResponse response, SignInRequest signInRequest);
    ResponseObject signUp(SignUpRequest signUpRequest);
    ResponseObject logout(HttpServletRequest request, HttpServletResponse response, UserPrincipal currentUser);
    ResponseObject refeshToken(HttpServletRequest request, HttpServletResponse response);
    ResponseObject verificationCredential(boolean isOtp, String verify);
    ResponseObject reissueVerificationCredential(ReissueVerificationCredentialRequest request);
    ResponseObject changeUserPassword(ChangeUserPasswordRequest request);
}
