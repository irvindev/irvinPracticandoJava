package com.pe.allpafood.api.core.utils.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class ResponseWrapDTO<T> {
    private int code;
    private String message;
    private T data;
}