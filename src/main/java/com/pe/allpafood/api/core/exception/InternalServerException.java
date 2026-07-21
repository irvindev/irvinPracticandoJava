package com.pe.allpafood.api.core.exception;

public class InternalServerException extends RuntimeException {

    public InternalServerException(String message) {
        super(message);
    }
}