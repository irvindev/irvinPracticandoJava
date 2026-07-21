package com.pe.allpafood.api.transaction.catalog.entity;

import lombok.Data;

@Data
public class MenuTypeEntity {
    private Integer id;
    private Integer menuId;
    private MenuEntity menu;
    private String type;
    private Integer status;
}
