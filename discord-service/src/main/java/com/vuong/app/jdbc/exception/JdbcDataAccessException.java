package com.vuong.app.jdbc.exception;

public class JdbcDataAccessException extends RuntimeException {
    public JdbcDataAccessException() {
    }

    public JdbcDataAccessException(String message) {
        super(message);
    }

    public JdbcDataAccessException(String message, Throwable cause) {
        super(message, cause);
    }

    public JdbcDataAccessException(Throwable cause) {
        super(cause);
    }

    public JdbcDataAccessException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
