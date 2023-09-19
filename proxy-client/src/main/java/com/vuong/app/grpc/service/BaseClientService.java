package com.vuong.app.grpc.service;

import com.google.protobuf.Any;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import com.vuong.app.exception.wrapper.ResourceNotFoundException;
import com.vuong.app.v1.message.GrpcErrorCode;
import com.vuong.app.v1.message.GrpcRequest;
import com.vuong.app.v1.message.GrpcResponse;

import java.util.Optional;

public abstract class BaseClientService {

    protected <T extends Message> Optional<T> unpackedResult(GrpcResponse response, Class<T> responseType) {

        GrpcResponse.ResponseCase responseCase = response.getResponseCase();

        if (responseCase.equals(GrpcResponse.ResponseCase.ERROR_RESPONSE)) {
            GrpcErrorCode errorCode = response.getErrorResponse().getErrorCode();
            String message = response.getErrorResponse().getMessage();

            if (errorCode.compareTo(GrpcErrorCode.ERROR_CODE_BAD_REQUEST) == 0) {
                throw new ResourceNotFoundException(message);
            }

            if (errorCode.compareTo(GrpcErrorCode.ERROR_CODE_NOT_FOUND) == 0) {
                return Optional.empty();
            }
        }

        // end handle error

        Any result = response.getSuccessResponse().getResult();
        if (!result.is(responseType)) {
            throw new RuntimeException("Error Response : " + response.getErrorResponse().getErrorCode());
        }

        T unpackedResult = null;

        try {
            unpackedResult = result.unpack(responseType);
        } catch (InvalidProtocolBufferException e) {
            throw new RuntimeException("Error Response : " + response.getErrorResponse().getErrorCode());
        }

        return Optional.of(unpackedResult);
    }

    protected <T extends Message> GrpcRequest packRequest(T request, String correlationId) {
        return GrpcRequest.newBuilder()
                .setCorrelationId(correlationId)
                .setRequest(Any.pack(request))
                .build();
    }

    protected <T extends Message> GrpcRequest packRequest(T request) {
        return GrpcRequest.newBuilder()
                .setRequest(Any.pack(request))
                .build();
    }

}
