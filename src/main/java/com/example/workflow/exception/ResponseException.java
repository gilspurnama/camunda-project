package com.example.workflow.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class ResponseException extends RuntimeException{
    private final Integer errorCode;
    private final HttpStatus status;

    public ResponseException(String message, HttpStatus status, Integer errorCode) {
        super(message);
        this.status = status;
        this.errorCode = errorCode;
    }

    public ResponseException(String message, Throwable throwable, HttpStatus status, Integer errorCode) {
        super(message,throwable);
        this.status = status;
        this.errorCode = errorCode;
    }
}
