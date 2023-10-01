package com.vuong.app.service;

import com.google.protobuf.Any;
import com.google.protobuf.FieldMask;
import com.google.protobuf.util.FieldMaskUtil;
import com.vuong.app.constant.AppConstant;
import com.vuong.app.doman.AuthProvider;
import com.vuong.app.doman.User;
import com.vuong.app.doman.User_;
import com.vuong.app.dto.user.UserFilterParameter;
import com.vuong.app.dto.user.UserSortParameter;
import com.vuong.app.jpa.query.PageInfo;
import com.vuong.app.jpa.query.QueryBuilder;
import com.vuong.app.jpa.query.QueryHelper;
import com.vuong.app.jpa.query.ServiceHelper;
import com.vuong.app.operator.StringOperators;
import com.vuong.app.v1.*;
import com.vuong.app.repository.UserRepository;
import com.vuong.app.v1.message.*;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.CollectionUtils;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import javax.transaction.Transactional;
import java.util.Optional;

@GrpcService
@Transactional
@Slf4j
@RequiredArgsConstructor
public class UserService extends UserServiceGrpc.UserServiceImplBase {

    private final UserRepository userRepository;
    private final VerificationCredentialService verificationCredentialService;

    @Override
    public void createUser(GrpcRequest request, StreamObserver<GrpcResponse> responseObserver) {
        GrpcCreateUserRequest req = ServiceHelper.unpackedRequest(request, GrpcCreateUserRequest.class);

        AuthProvider authProvider = AuthProvider.forNumber(req.getProvider().getNumber());
        Boolean verified = !(authProvider.getNumber() == AuthProvider.local.getNumber());

        User user = this.userRepository.save(User.builder()
                        .name(req.getName())
                        .avatar(req.getAvatar())
                        .email(req.getEmail())
                        .password(req.getPassword())
                        .verified(verified)
                        .provider(authProvider)
                        .providerId(req.getProviderId())
                .build());

        // send mail verify
        if (!verified) {
            this.verificationCredentialService.sendMailVerifyCreateUser(user);
        }

        GrpcCreateUserResponse response = GrpcCreateUserResponse.newBuilder().setUserId(user.getUserId()).build();

        ServiceHelper.next(responseObserver, ServiceHelper.packedSuccessResponse(response));
    }

    @Override
    public void updateUserByUserId(GrpcRequest request, StreamObserver<GrpcResponse> responseObserver) {
        GrpcUpdateUserByUserIdRequest req = ServiceHelper.unpackedRequest(request, GrpcUpdateUserByUserIdRequest.class);

        Optional<User> updateUserOptional = this.userRepository.findById(req.getUserId());

        if (!updateUserOptional.isPresent()) {
            ServiceHelper.next(responseObserver, ServiceHelper.packedErrorResponse(GrpcErrorCode.ERROR_CODE_NOT_FOUND, "user not found with id"));
            return;
        }

        User updateUser = updateUserOptional.get();
        updateUser.setName(req.getName());
        updateUser.setAvatar(req.getAvatar());
        updateUser.setBio(req.getBio());

        User user = this.userRepository.save(updateUser);

        GrpcUpdateUserByUserIdResponse response = GrpcUpdateUserByUserIdResponse.newBuilder().setUserId(user.getUserId()).build();

        ServiceHelper.next(responseObserver, ServiceHelper.packedSuccessResponse(response));
    }

    @Override
    public void changeUserPasswordByUserId(GrpcRequest request, StreamObserver<GrpcResponse> responseObserver) {
        GrpcChangeUserPasswordByUserIdRequest req = ServiceHelper.unpackedRequest(request, GrpcChangeUserPasswordByUserIdRequest.class);

        Optional<User> updateUserOptional = this.userRepository.findById(req.getUserId());

        if (!updateUserOptional.isPresent()) {
            ServiceHelper.next(responseObserver, ServiceHelper.packedErrorResponse(GrpcErrorCode.ERROR_CODE_NOT_FOUND, "user not found with id"));
            return;
        }

        User updateUser = updateUserOptional.get();

        if (!updateUser.getPassword().equals(req.getOldPassword())) {
            ServiceHelper.next(responseObserver, ServiceHelper.packedErrorResponse(GrpcErrorCode.ERROR_CODE_NOT_FOUND, "Old password wrong!"));
            return;
        }

        updateUser.setPassword(req.getNewPassword());

        User user = this.userRepository.save(updateUser);

        GrpcChangeUserPasswordByUserIdResponse response = GrpcChangeUserPasswordByUserIdResponse.newBuilder().setUserId(user.getUserId()).build();

        ServiceHelper.next(responseObserver, ServiceHelper.packedSuccessResponse(response));
    }

    @Override
    public void existsUserByEmail(GrpcRequest request, StreamObserver<GrpcResponse> responseObserver) {
        GrpcExistsUserByEmailRequest req = ServiceHelper.unpackedRequest(request, GrpcExistsUserByEmailRequest.class);

        boolean exists = this.userRepository.existsByEmail(req.getEmail());

        GrpcExistsUserByEmailResponse response = GrpcExistsUserByEmailResponse.newBuilder().setExists(exists).build();

        ServiceHelper.next(responseObserver, ServiceHelper.packedSuccessResponse(response));
    }

    @Override
    public void checkUserVerifiedByUserId(GrpcRequest request, StreamObserver<GrpcResponse> responseObserver) {
        GrpcCheckUserVerifiedByUserIdRequest req = ServiceHelper.unpackedRequest(request, GrpcCheckUserVerifiedByUserIdRequest.class);

        Optional<UserRepository.UserVerified> userVerifiedOptional = this.userRepository.findById(req.getUserId(), UserRepository.UserVerified.class);

        boolean verified =  false;

        if (userVerifiedOptional.isPresent()) {
            UserRepository.UserVerified userVerified = userVerifiedOptional.get();
            verified = userVerified.getVerified();
        }

        GrpcCheckUserVerifiedByUserIdResponse response = GrpcCheckUserVerifiedByUserIdResponse.newBuilder().setVerified(verified).build();

        ServiceHelper.next(responseObserver, ServiceHelper.packedSuccessResponse(response));
    }

    @Override
    public void getUserByUserId(GrpcRequest request, StreamObserver<GrpcResponse> responseObserver) {
        GrpcGetUserByUserIdRequest req = ServiceHelper.unpackedRequest(request, GrpcGetUserByUserIdRequest.class);

        Optional<UserRepository.UserWithoutVerificationCredential> userOptional = this.userRepository.findById(req.getUserId(), UserRepository.UserWithoutVerificationCredential.class);

        if (!userOptional.isPresent()) {
            ServiceHelper.next(responseObserver, ServiceHelper.packedErrorResponse(GrpcErrorCode.ERROR_CODE_NOT_FOUND, "user not found with id"));
            return;
        }

        UserRepository.UserWithoutVerificationCredential user = userOptional.get();

        GrpcGetUserByUserIdResponse response = GrpcGetUserByUserIdResponse.newBuilder().setUser(GrpcUser.newBuilder()
                .setUserId(user.getUserId())
                .setName(user.getEmail())
                .setAvatar(user.getAvatar())
                .setBio(user.getBio())
                .setEmail(user.getEmail())
                .setPassword(user.getPassword())
                .setVerified(user.getVerified())
                .setProvider(GrpcAuthProvider.forNumber(user.getProvider().getNumber()))
                .setProviderId(user.getProviderId())
                .setCreatedAt(user.getCreatedAt().toString())
                .setUpdatedAt(user.getUpdatedAt().toString())
                .build()).build();

        ServiceHelper.next(responseObserver, ServiceHelper.packedSuccessResponse(response));
    }

    @Override
    public void getUserByEmail(GrpcRequest request, StreamObserver<GrpcResponse> responseObserver) {
        GrpcGetUserByEmailRequest req = ServiceHelper.unpackedRequest(request, GrpcGetUserByEmailRequest.class);

        QueryBuilder queryBuilder = new QueryBuilder();

        StringOperators emailOperator = new StringOperators();
        emailOperator.setEq(req.getEmail());

        UserFilterParameter filterParameter = new UserFilterParameter();
        filterParameter.setEmail(emailOperator);

        this.buildFilter(queryBuilder, filterParameter);

        Optional<UserRepository.UserWithoutVerificationCredential> userOptional = this.userRepository.findOne(queryBuilder.build(), UserRepository.UserWithoutVerificationCredential.class);

        if (!userOptional.isPresent()) {
            ServiceHelper.next(responseObserver, ServiceHelper.packedErrorResponse(GrpcErrorCode.ERROR_CODE_NOT_FOUND, "user not found with email"));
            return;
        }

        UserRepository.UserWithoutVerificationCredential user = userOptional.get();

        GrpcGetUserByEmailResponse response = GrpcGetUserByEmailResponse.newBuilder().setUser(GrpcUser.newBuilder()
                .setUserId(user.getUserId())
                .setName(user.getEmail())
                .setAvatar(user.getAvatar())
                .setBio(user.getBio())
                .setEmail(user.getEmail())
                .setPassword(user.getPassword())
                .setVerified(user.getVerified())
                .setProvider(GrpcAuthProvider.forNumber(user.getProvider().getNumber()))
                .setProviderId(user.getProviderId())
                .setCreatedAt(user.getCreatedAt().toString())
                .setUpdatedAt(user.getUpdatedAt().toString())
                .build()).build();

        ServiceHelper.next(responseObserver, ServiceHelper.packedSuccessResponse(response));
    }

    private void buildSortOrder(QueryBuilder queryBuilder, UserSortParameter sortParameter) {
        if (sortParameter == null) return ;
        QueryHelper.buildOneSortOrder(queryBuilder, sortParameter.getCreatedAt(), User_.CREATED_AT);
        QueryHelper.buildOneSortOrder(queryBuilder, sortParameter.getUpdatedAt(), User_.UPDATED_AT);
    }

    private void buildFilter(QueryBuilder queryBuilder, UserFilterParameter filterParameter) {
        if (filterParameter == null) return;
        QueryHelper.buildOneNumberOperatorFilter(queryBuilder, filterParameter.getUserId(), User_.USER_ID);
        QueryHelper.buildOneStringOperatorFilter(queryBuilder, filterParameter.getName(), User_.NAME);
        QueryHelper.buildOneStringOperatorFilter(queryBuilder, filterParameter.getEmail(), User_.EMAIL);
        QueryHelper.buildOneBooleanOperatorFilter(queryBuilder, filterParameter.getVerified(), User_.VERIFIED);
        QueryHelper.buildOneDateOperatorFilter(queryBuilder, filterParameter.getCreatedAt(), User_.CREATED_AT);
        QueryHelper.buildOneDateOperatorFilter(queryBuilder, filterParameter.getUpdatedAt(), User_.UPDATED_AT);
    }

    //    @Override
//    public void grpcCreate(GrpcRequest request, StreamObserver<GrpcResponse> responseObserver) {
//
//        GrpcCreateUserRequest req = ServiceHelper.unpackedRequest(request, GrpcCreateUserRequest.class);
//
//        User user = this.userRepository.save(User.builder()
//                        .name(req.getName())
//                        .avatar(req.getAvatar())
//                        .email(req.getEmail())
//                        .password(req.getPassword())
//                        .provider(AuthProvider.forNumber(req.getProvider().getNumber()))
//                        .providerId(req.getProviderId())
//                .build());
//        GrpcCreateUserResponse grpcCreateUserResponse = GrpcCreateUserResponse.newBuilder().setUserId(user.getUserId()).build();
//
//        GrpcResponse.Builder builderResponse = GrpcResponse.newBuilder();
//
//        builderResponse.setSuccessResponse(GrpcSuccessResponse.newBuilder()
//                .setResult(Any.pack(grpcCreateUserResponse))
//                .build());
//
//        GrpcResponse response = builderResponse.build();
//
//        responseObserver.onNext(response);
//        responseObserver.onCompleted();
//    }
//
//    @Override
//    public void grpcUpdate(GrpcRequest request, StreamObserver<GrpcResponse> responseObserver) {
//        GrpcUpdateUserRequest req = ServiceHelper.unpackedRequest(request, GrpcUpdateUserRequest.class);
//
//        GrpcUserUpdateOperation userUpdateOperation = req.getUpdate();
//
//        Optional<User> userOptional = this.userRepository.findById(req.getUserId());
//
//        GrpcResponse.Builder builderResponse = GrpcResponse.newBuilder();
//
//        if (userOptional.isPresent()) {
//            User user = userOptional.get();
//
//            GrpcUserUpdateOperation current = GrpcUserUpdateOperation.newBuilder()
//                    .setName(user.getName())
//                    .setAvatar(user.getAvatar())
//                    .setBio(user.getBio())
//                    .setPassword(user.getPassword())
//                    .build();
//
//            FieldMask updateMask = req.getUpdateMask();
//            GrpcUserUpdateOperation.Builder updated = current.toBuilder();
//
//            FieldMaskUtil.merge(updateMask, userUpdateOperation, updated);
//
//            user = this.userRepository.save(User.builder()
//                    .userId(user.getUserId())
//                    .name(updated.getName())
//                    .avatar(updated.getAvatar())
//                    .bio(updated.getBio())
//                    .build());
//
//            GrpcUpdateUserResponse updateUserResponse = GrpcUpdateUserResponse.newBuilder().setUserId(user.getUserId()).build();
//
//            builderResponse.setSuccessResponse(GrpcSuccessResponse.newBuilder()
//                    .setResult(Any.pack(updateUserResponse))
//                    .build());
//        } else {
//            builderResponse.setErrorResponse(GrpcErrorResponse.newBuilder()
//                    .setErrorCode(GrpcErrorCode.ERROR_CODE_BAD_REQUEST)
//                    .setMessage("Cannot get user by id")
//                    .build());
//        }
//
//        GrpcResponse response = builderResponse.build();
//
//        responseObserver.onNext(response);
//        responseObserver.onCompleted();
//    }
//
//    @Override
//    public void grpcFindAll(GrpcRequest request, StreamObserver<GrpcResponse> responseObserver) {
//        GrpcUserListOptionsRequest req = ServiceHelper.unpackedRequest(request, GrpcUserListOptionsRequest.class);
//
//        PageInfo pageInfo = this.getListOptions(req);
//
//        QueryBuilder queryBuilder = new QueryBuilder();
//
//        this.buildFilter(queryBuilder, req);
//        this.buildSortOrder(queryBuilder, req);
//
////        queryBuilder.query((root, query, criteriaBuilder) -> {
////            query.multiselect(criteriaBuilder.construct(User.class, root.get(User_.NAME), root.get(User_.BIO)));
////            return null;
////        });
//
//        // query.multiselect(criteriaBuilder.construct(User.class, root.get(User_.NAME), root.get(User_.BIO))); không hoạt động với findAll của Specification
//        Page<UserRepository.UserTest> page = this.userRepository.findAll(queryBuilder.build(), UserRepository.UserTest.class, PageRequest.of(pageInfo.current, pageInfo.size));
//
//        GrpcUserPaginatedResponse.Builder userPaginatedResponseBuilder = GrpcUserPaginatedResponse.newBuilder();
//        userPaginatedResponseBuilder.setTotalItems((int) page.getTotalElements());
//
//        if (!CollectionUtils.isEmpty(page.getContent())) {
//            FieldMask fieldMask = req.getFieldMask();
//            GrpcUserDto.Builder filteredItem = GrpcUserDto.newBuilder();
//
//            page.forEach(user -> {
//                GrpcUserDto userResponse = GrpcUserDto.newBuilder()
////                        .setId(user.getUserId())
//                        .setName(user.getName())
////                        .setAvatar(user.getAvatar())
//                        .setBio(user.getBio())
//                        .build();
//
//                FieldMaskUtil.merge(fieldMask, userResponse, filteredItem);
//
//                userPaginatedResponseBuilder.addItems(filteredItem);
//            });
//        }
//
//        GrpcUserPaginatedResponse userPaginatedResponse = userPaginatedResponseBuilder.build();
//
//        GrpcResponse.Builder builderResponse = GrpcResponse.newBuilder();
//
//        builderResponse.setSuccessResponse(GrpcSuccessResponse.newBuilder()
//                .setResult(Any.pack(userPaginatedResponse))
//                .build());
//
//        GrpcResponse response = builderResponse.build();
//
//        responseObserver.onNext(response);
//        responseObserver.onCompleted();
//    }
//
//    @Override
//    public void grpcFindById(GrpcRequest request, StreamObserver<GrpcResponse> responseObserver) {
//        GrpcGetUserByIdRequest req = ServiceHelper.unpackedRequest(request, GrpcGetUserByIdRequest.class);
//
//        Optional<User> optionalUser = this.userRepository.findById(req.getUserId());
//
//        GrpcResponse.Builder builderResponse = GrpcResponse.newBuilder();
//
//        if (optionalUser.isPresent()) {
//            User user = optionalUser.get();
//
//            GrpcUserDto userResponse = GrpcUserDto.newBuilder()
//                    .setUserId(user.getUserId())
//                    .setName(user.getName())
//                    .setAvatar(user.getAvatar())
//                    .setBio(user.getBio())
//                    .setEmail(user.getEmail())
//                    .setPassword(user.getPassword())
//                    .setProvider(GrpcAuthProvider.forNumber(user.getProvider().getNumber()))
//                    .setProviderId(user.getProviderId())
//                    .build();
//
//            FieldMask fieldMask = req.getFieldMask();
//
//            GrpcUserDto.Builder filteredItem = GrpcUserDto.newBuilder();
//
//            FieldMaskUtil.merge(fieldMask, userResponse, filteredItem);
//
//            GrpcGetUserByIdResponse getUserResponse = GrpcGetUserByIdResponse.newBuilder()
//                    .setUser(filteredItem).build();
//
//            builderResponse.setSuccessResponse(GrpcSuccessResponse.newBuilder()
//                    .setResult(Any.pack(getUserResponse))
//                    .build());
//        } else {
//            builderResponse.setErrorResponse(GrpcErrorResponse.newBuilder()
//                    .setErrorCode(GrpcErrorCode.ERROR_CODE_NOT_FOUND)
//                    .setMessage("Cannot get user by id")
//                    .build());
//        }
//
//        GrpcResponse response = builderResponse.build();
//
//        responseObserver.onNext(response);
//        responseObserver.onCompleted();
//    }
//
//    @Override
//    public void grpcFindByEmail(GrpcRequest request, StreamObserver<GrpcResponse> responseObserver) {
//        GrpcGetUserByEmailRequest req = ServiceHelper.unpackedRequest(request, GrpcGetUserByEmailRequest.class);
//
//        Optional<User> optionalUser = this.userRepository.findByEmail(req.getEmail());
//
//        GrpcResponse.Builder builderResponse = GrpcResponse.newBuilder();
//
//        if (optionalUser.isPresent()) {
//            User user = optionalUser.get();
//
//            GrpcUserDto userResponse = GrpcUserDto.newBuilder()
//                    .setUserId(user.getUserId())
//                    .setName(user.getName())
//                    .setAvatar(user.getAvatar())
//                    .setBio(user.getBio())
//                    .setEmail(user.getEmail())
//                    .setPassword(user.getPassword())
//                    .setProvider(GrpcAuthProvider.forNumber(user.getProvider().getNumber()))
//                    .setProviderId(user.getProviderId())
//                    .build();
//
//            FieldMask fieldMask = req.getFieldMask();
//
//            GrpcUserDto.Builder filteredItem = GrpcUserDto.newBuilder();
//
//            FieldMaskUtil.merge(fieldMask, userResponse, filteredItem);
//
//            GrpcGetUserByEmailResponse getUserResponse = GrpcGetUserByEmailResponse.newBuilder()
//                    .setUser(filteredItem).build();
//
//            builderResponse.setSuccessResponse(GrpcSuccessResponse.newBuilder()
//                    .setResult(Any.pack(getUserResponse))
//                    .build());
//        } else {
//            builderResponse.setErrorResponse(GrpcErrorResponse.newBuilder()
//                    .setErrorCode(GrpcErrorCode.ERROR_CODE_NOT_FOUND)
//                    .setMessage("Cannot get user by id")
//                    .build());
//        }
//
//        GrpcResponse response = builderResponse.build();
//
//        responseObserver.onNext(response);
//        responseObserver.onCompleted();
//    }
//
//    @Override
//    public void grpcExistsByEmail(GrpcRequest request, StreamObserver<GrpcResponse> responseObserver) {
//        GrpcExistsByEmailRequest req = ServiceHelper.unpackedRequest(request, GrpcExistsByEmailRequest.class);
//        boolean exists = this.userRepository.existsByEmail(req.getEmail());
//
//        GrpcExistsByEmailResponse existsByEmailResponse = GrpcExistsByEmailResponse.newBuilder()
//                .setExists(exists).build();
//
//        GrpcResponse.Builder builderResponse = GrpcResponse.newBuilder();
//
//        builderResponse.setSuccessResponse(GrpcSuccessResponse.newBuilder()
//                .setResult(Any.pack(existsByEmailResponse))
//                .build());
//
//        GrpcResponse response = builderResponse.build();
//
//        responseObserver.onNext(response);
//        responseObserver.onCompleted();
//    }
//
//    private void buildSortOrder(QueryBuilder queryBuilder, GrpcUserListOptionsRequest request) {
//        if (!request.hasSort()) {
//            return;
//        }
//
//        GrpcUserSortParameter sortParameter = request.getSort();
//
//        QueryHelper.buildOneSortOrder(queryBuilder, sortParameter.getCreatedAt(), User_.CREATED_AT);
//        QueryHelper.buildOneSortOrder(queryBuilder, sortParameter.getUpdatedAt(), User_.UPDATED_AT);
//    }
//
//    private void buildFilter(QueryBuilder queryBuilder, GrpcUserListOptionsRequest request) {
//        if (!request.hasFilter()) {
//            return;
//        }
//
//        GrpcUserFilterParameter filterParameter = request.getFilter();
//
//        QueryHelper.buildOneStringOperatorFilter(queryBuilder, filterParameter.getName(), User_.NAME);
//        QueryHelper.buildOneStringOperatorFilter(queryBuilder, filterParameter.getAvatar(), User_.AVATAR);
//        QueryHelper.buildOneStringOperatorFilter(queryBuilder, filterParameter.getBio(), User_.BIO);
//    }
//
//    private Specification<User> getById(Integer userId) {
//        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get(User_.USER_ID), userId);
//    }
//
//    public static PageInfo getListOptions(GrpcUserListOptionsRequest request) {
//        int currentPage = AppConstant.DEFAULT_CURRENT_PAGE;
//        int pageSize = AppConstant.DEFAULT_PAGE_SIZE;
//
////        if (request.hasCurrentPage()) {
////            currentPage = request.getCurrentPage();
////        }
////
////        if (request.hasPageSize()) {
////            pageSize = request.getPageSize();
////        }
//
//        return PageInfo.builder().current(currentPage).size(pageSize).build();
//    }
}
