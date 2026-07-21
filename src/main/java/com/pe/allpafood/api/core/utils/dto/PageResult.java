package com.pe.allpafood.api.core.utils.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.util.List;
@Data
public class PageResult<T> {
    private List<T> content;
    private int page;
    private int size;
    private long totalElements;

    public PageResult(List<T> content, int page, int size, long totalElements) {
        this.content = content;
        this.page = page;
        this.size = size;
        this.totalElements = totalElements;
    }

    // Getters y setters
}
