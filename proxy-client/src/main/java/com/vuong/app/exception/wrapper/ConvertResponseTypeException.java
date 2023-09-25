package com.vuong.app.exception.wrapper;

public class ConvertResponseTypeException extends RuntimeException {
    public ConvertResponseTypeException() {
        super("Convert data type wrong");
    }

    public ConvertResponseTypeException(String message) {
        super(message);
    }

    public ConvertResponseTypeException(String message, Throwable cause) {
        super(message, cause);
    }

    public ConvertResponseTypeException(Throwable cause) {
        super(cause);
    }

    public ConvertResponseTypeException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
