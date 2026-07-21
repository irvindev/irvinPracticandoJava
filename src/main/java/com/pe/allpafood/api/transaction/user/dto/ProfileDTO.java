package com.pe.allpafood.api.transaction.user.dto;

import com.pe.allpafood.api.core.utils.dto.GeoPoint;
import com.pe.allpafood.api.transaction.user.entities.InformationEntity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

public record ProfileDTO(
        @NotNull(message = "{error.bornDate.notnull}")
        @DateTimeFormat(pattern = "yyyy-MM-dd")
        LocalDate bornDate,

        @NotNull(message = "{error.district.notnull}")
        @NotBlank(message = "{error.district.notblank}")
        String district,

        @NotNull(message = "{error.address.notnull}")
        @NotBlank(message = "{error.address.notblank}")
        String address,

        @NotNull(message = "{error.descriptionAddress.notnull}")
        @NotBlank(message = "{error.descriptionAddress.notblank}")
        String descriptionAddress,

        @NotNull(message = "{error.location.notnull}")
        GeoPoint location,
        @NotNull(message = "{error.district.notnull}")
        @NotBlank(message = "{error.district.notblank}")
        String districtLocation,

        @NotNull(message = "{error.information.notnull}")
        InformationEntity information,
        @Null
        String name,

        String image
){
}
