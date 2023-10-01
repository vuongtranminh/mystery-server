package com.vuong.app.common.api;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;

@Getter
@Setter
public class ResponseMsg extends ResponseObject {
    private Object data;

    public ResponseMsg(Object message, HttpStatus httpStatus, Object data) {
        super(true, message, httpStatus);
        this.data = data;
    }

    public ResponseMsg(Object message, HttpStatus httpStatus) {
        super(true, message, httpStatus);
    }
}
