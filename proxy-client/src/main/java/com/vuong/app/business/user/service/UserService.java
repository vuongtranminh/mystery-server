package com.vuong.app.business.user.service;

import com.vuong.app.business.user.model.UserDto;

public interface UserService {
    UserDto getUserById(Integer userId);
}
