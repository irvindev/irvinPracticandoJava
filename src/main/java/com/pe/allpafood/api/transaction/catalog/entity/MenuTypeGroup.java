package com.pe.allpafood.api.transaction.catalog.entity;

import lombok.Data;

import java.util.List;

@Data
public class MenuTypeGroup {
    private String type;
    private List<MenuTypeEntity> menuTypes;
}
