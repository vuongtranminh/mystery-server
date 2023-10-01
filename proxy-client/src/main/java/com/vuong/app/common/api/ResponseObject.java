package com.vuong.app.common.api;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.ZonedDateTimeSerializer;
import lombok.*;
import org.springframework.http.HttpStatus;

import java.time.ZoneId;
import java.time.ZonedDateTime;

import static com.fasterxml.jackson.annotation.JsonFormat.Shape.STRING;
import static com.vuong.app.constant.AppConstant.ZONED_DATE_TIME_FORMAT;

@Getter
@Setter
public class ResponseObject {
    private boolean success;
    private Object message;

    private HttpStatus httpStatus;

    @JsonSerialize(using = ZonedDateTimeSerializer.class)
    @JsonFormat(shape = STRING, pattern = ZONED_DATE_TIME_FORMAT)
    private ZonedDateTime timestamp;

    public ResponseObject(boolean success, Object message, HttpStatus httpStatus) {
        this(success, message, httpStatus, ZonedDateTime.now(ZoneId.systemDefault()));
    }

    public ResponseObject(boolean success, Object message, HttpStatus httpStatus, ZonedDateTime timestamp) {
        this.success = success;
        this.message = message;
        this.httpStatus = httpStatus;
        this.timestamp = timestamp;
    }
}
