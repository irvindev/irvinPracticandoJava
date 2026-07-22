package com.pe.allpafood.api.transaction.catalog.entity;

import com.pe.allpafood.api.transaction.plan.entities.benefits.DetailEntity;
import lombok.Data;

import java.util.List;

@Data
public class MenuEntity {
    private Integer id;
    private String name;
    private String imageUrl;
    private String description;
    private Boolean selectable;
    private Float price;
    private Float previousPrice;
    private List<String> types;
    private List<MenuTypeEntity> menuTypes;
    private List<DetailEntity<Float>> properties;
    private String propertiesJson;

    private String extraBenefitsJson;
    private String principalBenefitsJson;

    public MenuEntity() {
    }

    public MenuEntity(Integer id) {
        this.id = id;
    }


}
