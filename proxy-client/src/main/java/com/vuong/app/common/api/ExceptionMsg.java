package com.vuong.app.common.api;

import org.springframework.http.HttpStatus;

public class ExceptionMsg extends ResponseObject {
    public ExceptionMsg(Object message, HttpStatus httpStatus) {
        super(false, message, httpStatus);
    }
}
