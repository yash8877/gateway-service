package com.gateway.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;


@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(JWTExpireException.class)
    public ResponseEntity<ErrorResponse> handleAuthenticationException(JWTExpireException ex) {
        return buildResponse(ex, HttpStatus.UNAUTHORIZED, "JWT ERROR");
    }

    @ExceptionHandler
    public ResponseEntity<ErrorResponse> handleInvalidJWTException(InvalidJWTException in){
        return buildResponse(in,HttpStatus.FORBIDDEN,"JWT ERROR");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleRuntimeException(RuntimeException ex) {
        return buildResponse(ex, HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error");
    }

    private ResponseEntity<ErrorResponse> buildResponse(Exception ex, HttpStatus status, String error) {
        ErrorResponse response = new ErrorResponse();
        response.setTimestamp(LocalDateTime.now());
        response.setError(error);
        response.setMessage(ex.getMessage());
        response.setStatuscode(status.value());
        return new ResponseEntity<>(response, status);
    }
}
