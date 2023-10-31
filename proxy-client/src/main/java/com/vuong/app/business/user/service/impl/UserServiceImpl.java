package com.vuong.app.business.user.service.impl;

import com.vuong.app.business.user.model.payload.UserSummary;
import com.vuong.app.business.user.service.UserService;
import com.vuong.app.common.api.ResponseMsg;
import com.vuong.app.common.api.ResponseObject;
import com.vuong.app.exception.wrapper.ResourceNotFoundException;
import com.vuong.app.grpc.message.auth.GetUserByUserIdRequest;
import com.vuong.app.grpc.message.auth.GetUserByUserIdResponse;
import com.vuong.app.grpc.service.UserClientService;
import com.vuong.app.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserClientService userClientService;

    @Override
    public ResponseObject getCurrentUser(UserPrincipal currentUser) {
        Optional<GetUserByUserIdResponse> getUserByUserIdResponseOptional = this.userClientService.getUserByUserId(GetUserByUserIdRequest.builder()
                        .userId(currentUser.getUserId())
                .build());

        if (!getUserByUserIdResponseOptional.isPresent()) {
            throw new ResourceNotFoundException("user", "user_id", currentUser.getUserId());
        }

        GetUserByUserIdResponse getUserByUserIdResponse = getUserByUserIdResponseOptional.get();

        UserSummary userSummary = UserSummary.builder()
                .userId(getUserByUserIdResponse.getUserId())
                .name(getUserByUserIdResponse.getName())
                .avtUrl(getUserByUserIdResponse.getAvtUrl())
                .build();

        return new ResponseMsg("success", HttpStatus.OK, userSummary);
    }

//    @Override
//    public UserDto getUserById(Integer userId) {
//        Optional<GetUserByIdResponse> getUserByIdResponseOptional = this.userClientService.getUserById(GetUserByIdRequest.builder()
//                .userId(userId)
//                .build());
//
//        if (!getUserByIdResponseOptional.isPresent()) {
//            throw new RuntimeException("Not found");
//        }
//
//        GetUserByIdResponse getUserByIdResponse = getUserByIdResponseOptional.get();
//
//        return UserDto.builder()
//                .name(getUserByIdResponse.getName())
//                .avatar(getUserByIdResponse.getAvatar())
//                .bio(getUserByIdResponse.getBio())
//                .email(getUserByIdResponse.getEmail())
//                .build();
//    }
}
