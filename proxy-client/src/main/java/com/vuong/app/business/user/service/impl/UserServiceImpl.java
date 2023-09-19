package com.vuong.app.business.user.service.impl;

import com.vuong.app.business.user.model.UserDto;
import com.vuong.app.business.user.service.UserService;
import com.vuong.app.grpc.message.user.GetUserByIdRequest;
import com.vuong.app.grpc.message.user.GetUserByIdResponse;
import com.vuong.app.grpc.service.UserClientService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserClientService userClientService;

    @Override
    public UserDto getUserById(Integer userId) {
        Optional<GetUserByIdResponse> getUserByIdResponseOptional = this.userClientService.getUserById(GetUserByIdRequest.builder()
                .userId(userId)
                .build());

        if (!getUserByIdResponseOptional.isPresent()) {
            throw new RuntimeException("Not found");
        }

        GetUserByIdResponse getUserByIdResponse = getUserByIdResponseOptional.get();

        return UserDto.builder()
                .name(getUserByIdResponse.getName())
                .avatar(getUserByIdResponse.getAvatar())
                .bio(getUserByIdResponse.getBio())
                .email(getUserByIdResponse.getEmail())
                .build();
    }
}
