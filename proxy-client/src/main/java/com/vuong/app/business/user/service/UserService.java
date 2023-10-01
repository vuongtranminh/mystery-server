package com.vuong.app.business.user.service;

import com.vuong.app.common.api.ResponseObject;
import com.vuong.app.security.UserPrincipal;

public interface UserService {
    ResponseObject getCurrentUser(UserPrincipal currentUser);
}
