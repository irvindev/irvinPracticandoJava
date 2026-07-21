package com.pe.allpafood.api.transaction.catalog.dto.comanda;

import com.pe.allpafood.api.transaction.plan.entities.benefits.DetailEntity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record MenuTypeDTO(
        Integer id,

        @NotNull(message = "El nombre del plato es obligatorio.")
        @NotBlank(message = "El nombre del plato es obligatorio.")
        String name,

        @NotNull(message = "La descripcion del plato es obligatorio.")
        @NotBlank(message = "La descripcion del plato es obligatorio.")
        String description,

        @NotNull(message = "Las propiedades del plato es obligatorio.")
        List<DetailEntity<Float>> properties

) {
}
