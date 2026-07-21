package com.pe.allpafood.api.transaction.plan.entities.benefits;


import lombok.Data;


@Data
public class DetailEntity<T> {
    private String id;
    private String name;
    private T value;
    public DetailEntity() {

    }

    public DetailEntity(String id, String name, T value) {
        this.id = id;
        this.name = name;
        this.value = value;
    }

    public DetailEntity(String name, T value) {
        this.name = name;
        this.value = value;
    }
}
