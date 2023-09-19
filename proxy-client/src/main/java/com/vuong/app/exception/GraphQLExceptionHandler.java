package com.vuong.app.exception;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.vuong.app.exception.payload.ExceptionMsg;
import com.vuong.app.exception.wrapper.ResourceNotFoundException;
import graphql.GraphQLError;
import graphql.GraphqlErrorBuilder;
import graphql.schema.DataFetchingEnvironment;
import org.springframework.graphql.execution.DataFetcherExceptionResolverAdapter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.stereotype.Component;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@Component
@RequiredArgsConstructor
public class GraphQLExceptionHandler extends DataFetcherExceptionResolverAdapter {

    @Override
    public void setThreadLocalContextAware(boolean threadLocalContextAware) {
        super.setThreadLocalContextAware(threadLocalContextAware);
    }

    @Override
    public boolean isThreadLocalContextAware() {
        return super.isThreadLocalContextAware();
    }

    @Override
    protected List<GraphQLError> resolveToMultipleErrors(Throwable ex, DataFetchingEnvironment env) {
        return super.resolveToMultipleErrors(ex, env);
    }

    @Override
    protected GraphQLError resolveToSingleError(Throwable ex, DataFetchingEnvironment env) {
        return super.resolveToSingleError(ex, env);
//        var errorCode = ex.getErrorCode();
//        var message = resourceNotFoundException.getMessage();
//        return GraphqlErrorBuilder.newError(env)
//                .errorType(CustomErrorType.RESOURCE_NOT_FOUND)
//                .message(message)
//                .extensions(Map.of("errorCode", errorCode.getShortCode()))
//                .build();
    }
}