package com.pe.allpafood.api.core.utils.dto;



import java.time.LocalDateTime;

public record ErrorDTO (
        Integer code,

        String message,

        LocalDateTime timeStamp)
{
    @Override
    public String toString() {
        return "ErrorDTO{" +
                "code=" + code +
                ", message='" + message + '\'' +
                ", timeStamp=" + timeStamp +
                '}';
    }
}
