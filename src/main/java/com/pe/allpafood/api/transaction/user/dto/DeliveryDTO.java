package com.pe.allpafood.api.transaction.user.dto;

import com.pe.allpafood.api.core.utils.dto.GeoPoint;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;

public record DeliveryDTO(
        @Null
        Long id,
        @Null
        boolean assigned,
        @NotNull
        String address,
        @NotNull
        String description,
        @NotNull
        GeoPoint location,
        @NotNull
        String district
        ) {
}
