package com.vuong.app.business.auth.service;

import com.vuong.app.business.auth.model.payload.ChangeUserPasswordRequest;
import com.vuong.app.business.auth.model.payload.ReissueVerificationCredentialRequest;
import com.vuong.app.business.auth.model.payload.SignInRequest;
import com.vuong.app.business.auth.model.payload.SignUpRequest;
import com.vuong.app.common.api.ResponseObject;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface AuthService {

//    boolean existsByEmail(String email);

    ResponseObject signIn(SignInRequest signInRequest, HttpServletResponse response);
    ResponseObject signUp(SignUpRequest signUpRequest);
    ResponseObject logout(HttpServletRequest request, HttpServletResponse response);
    ResponseObject refeshToken(HttpServletRequest request, HttpServletResponse response);
    ResponseObject verificationCredential(boolean isOtp, String verify);
    ResponseObject reissueVerificationCredential(ReissueVerificationCredentialRequest request);
    ResponseObject changeUserPassword(ChangeUserPasswordRequest request);
}
