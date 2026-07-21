package com.pe.allpafood.api.core.enums;

import lombok.Getter;

@Getter
public enum StatusDeliveryEnum {

    COMPLETED("C", "COMPLETED"),
    PENDING("P", "PENDING"),
    IN_PROGRESS("I","IN PROGRESS"),
    CANCELLED("X", "CANCELLED");

    private final String id;
    private final String description;

    StatusDeliveryEnum(String id, String description) {
        this.id = id;
        this.description = description;
    }

    public static String fromId(String id) {
        for (StatusDeliveryEnum status : values()) {
            if (status.getId().equals(id)) {
                return status.description;
            }
        }
        throw new IllegalArgumentException("No existe un OrderStatus con el ID: " + id);
    }
}
