package com.gateway.exception;

public class JWTExpireException extends RuntimeException{
    public JWTExpireException(String msg){
        super(msg);
    }
}
