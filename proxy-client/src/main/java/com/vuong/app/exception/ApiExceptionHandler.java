package com.vuong.app.exception;

import com.vuong.app.common.api.ExceptionMsg;
import com.vuong.app.exception.wrapper.CommandException;
import com.vuong.app.exception.wrapper.ConvertResponseTypeException;
import com.vuong.app.exception.wrapper.WTuxException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
@Slf4j
@RequiredArgsConstructor
public class ApiExceptionHandler {

    @ExceptionHandler(value = { ConvertResponseTypeException.class })
    public <T extends RuntimeException> ResponseEntity<ExceptionMsg> handleConvertResponseTypeException(final T e) {

        log.info("**ApiExceptionHandler controller, handle API request*\n");

        return new ResponseEntity<>(
                new ExceptionMsg("#### " + e.getMessage() + "! ####", HttpStatus.INTERNAL_SERVER_ERROR),
                HttpStatus.INTERNAL_SERVER_ERROR
        );
    }

    @ExceptionHandler(value = { CommandException.class })
    public <T extends RuntimeException> ResponseEntity<ExceptionMsg> handleCommandException(final T e) {

        log.info("**ApiExceptionHandler controller, handle API request*\n");

        return new ResponseEntity<>(
                new ExceptionMsg("#### " + e.getMessage() + "! ####", HttpStatus.INTERNAL_SERVER_ERROR),
                HttpStatus.INTERNAL_SERVER_ERROR
        );
    }

    @ExceptionHandler(value = { ResourceNotFoundException.class })
    public <T extends RuntimeException> ResponseEntity<ExceptionMsg> handleResourceNotFoundException(final T e) {

        log.info("**ApiExceptionHandler controller, handle API request*\n");

        return new ResponseEntity<>(
                new ExceptionMsg("#### " + e.getMessage() + "! ####", HttpStatus.NOT_FOUND),
                HttpStatus.NOT_FOUND
        );
    }

    @ExceptionHandler(value = { WTuxException.class })
    public <T extends RuntimeException> ResponseEntity<ExceptionMsg> handleWTuxException(final T e) {

        log.info("**ApiExceptionHandler controller, handle API request*\n");

        return new ResponseEntity<>(
                new ExceptionMsg("#### " + e.getMessage() + "! ####", HttpStatus.NOT_FOUND),
                HttpStatus.NOT_FOUND
        );
    }
}
