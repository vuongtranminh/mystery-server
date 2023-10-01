package com.vuong.app.jpa.query;

import com.google.protobuf.Any;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import com.vuong.app.v1.message.*;
import io.grpc.stub.StreamObserver;

public class ServiceHelper {

    private static final int DEFAULT_CURRENT_PAGE = 0;
    private static final int DEFAULT_PAGE_SIZE = 30;

    public static PageInfo getListOptions(int currentPage, int pageSize) {
        if (currentPage <= 0) {
            currentPage = DEFAULT_CURRENT_PAGE;
        }
        if (pageSize <= 0) {
            pageSize = DEFAULT_PAGE_SIZE;
        }
        return PageInfo.builder().current(currentPage).size(pageSize).build();
    }

    public static <T extends Message> T unpackedRequest(GrpcRequest grpcRequest, Class<T> requestType) {

        Any request = grpcRequest.getRequest();
        if (!request.is(requestType)) {
            throw new RuntimeException("Error Request");
        }

        T unpackedRequest = null;

        try {
            unpackedRequest = request.unpack(requestType);
        } catch (InvalidProtocolBufferException e) {
            throw new RuntimeException("Error Request");
        }

        return unpackedRequest;
    }

    public static GrpcResponse packedSuccessResponse(Message message) {

        GrpcResponse.Builder builderResponse = GrpcResponse.newBuilder();

        builderResponse.setSuccessResponse(GrpcSuccessResponse.newBuilder()
                .setResult(Any.pack(message))
                .build());

        GrpcResponse response = builderResponse.build();

        return response;
    }

    public static GrpcResponse packedErrorResponse(GrpcErrorCode errorCode, String message) {

        GrpcResponse.Builder builderResponse = GrpcResponse.newBuilder();

        builderResponse.setErrorResponse(GrpcErrorResponse.newBuilder()
                    .setErrorCode(errorCode)
                    .setMessage(message)
                    .build());

        GrpcResponse response = builderResponse.build();

        return response;
    }

    public static void next(StreamObserver<GrpcResponse> responseObserver, GrpcResponse response) {
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

}
