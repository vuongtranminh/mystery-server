package com.vuong.app.exception.wrapper;

import com.vuong.app.v1.GrpcErrorResponse;

public class WTuxException extends RuntimeException{
    private int errorCode;
    private String errorCodeName;
    private String message;

    public WTuxException(GrpcErrorResponse errorResponse) {
        super();
        this.errorCode = errorResponse.getErrorCode().getNumber();
        this.errorCodeName = errorResponse.getErrorCode().name();
        this.message = errorResponse.getMessage();
    }

    public WTuxException(String message) {
        super(message);
    }

    public WTuxException(String message, Throwable cause) {
        super(message, cause);
    }

    public WTuxException(Throwable cause) {
        super(cause);
    }

    protected WTuxException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
