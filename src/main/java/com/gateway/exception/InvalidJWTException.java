package com.gateway.exception;

public class InvalidJWTException extends RuntimeException{
    public InvalidJWTException(String message){
        super(message);
    }
}
