package com.vuong.app.business.user.helper;

import com.google.protobuf.FieldMask;
import com.vuong.app.business.user.model.UserListOptions;
import com.vuong.app.operator.OperatorGrpcUtil;
import com.vuong.app.v1.GrpcUserFilterParameter;
import com.vuong.app.v1.GrpcUserListOptionsRequest;
import com.vuong.app.v1.GrpcUserSortParameter;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.DataFetchingFieldSelectionSet;
import graphql.schema.SelectedField;

import java.util.Set;
import java.util.stream.Collectors;

public class UserMappingHelper {
    
    public static GrpcUserListOptionsRequest map(UserListOptions options, DataFetchingEnvironment env) {

        DataFetchingFieldSelectionSet selectionSet = env.getSelectionSet();

        Set<String> requestedFields = env.getSelectionSet().getFields().stream()
                .map(SelectedField::getName)
                .collect(Collectors.toSet());

        GrpcUserSortParameter sortParameter = GrpcUserSortParameter.newBuilder().build();

        if (options.getSort() != null) {
            sortParameter = GrpcUserSortParameter.newBuilder()
                    .setCreatedAt(OperatorGrpcUtil.parseSortOrder(options.getSort().getCreatedAt()))
                    .setUpdatedAt(OperatorGrpcUtil.parseSortOrder(options.getSort().getUpdatedAt()))
                    .build();
        }

        GrpcUserFilterParameter userFilterParameter = GrpcUserFilterParameter.newBuilder().build();

        if (options.getFilter() != null) {
            userFilterParameter = GrpcUserFilterParameter.newBuilder()
                    .setName(OperatorGrpcUtil.parseStringOperators(options.getFilter().getName()))
                    .setAvatar(OperatorGrpcUtil.parseStringOperators(options.getFilter().getAvatar()))
                    .setBio(OperatorGrpcUtil.parseStringOperators(options.getFilter().getBio()))
                    .build();
        }

        return GrpcUserListOptionsRequest.newBuilder()
                .setCurrentPage(options.getCurrentPage())
                .setPageSize(options.getPageSize())
                .setSort(sortParameter)
                .setFilter(userFilterParameter)
                .setFieldMask(FieldMask.newBuilder()
                        .addAllPaths(requestedFields)
                        .build())
                .build();
    }
}
