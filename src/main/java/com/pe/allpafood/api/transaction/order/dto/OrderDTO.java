package com.pe.allpafood.api.transaction.order.dto;

import com.pe.allpafood.api.transaction.catalog.entity.MenuEntity;

import java.time.LocalDate;
import java.util.List;

import com.pe.allpafood.api.transaction.catalog.entity.MenuTypeEntity;
import io.swagger.v3.oas.annotations.media.Schema;


@Schema(description = "Orden de menú del usuario")
public record OrderDTO(

        @Schema(description = "ID de la orden", example = "5678")
        Long id,

        @Schema(description = "Fecha de la orden", example = "2025-05-06")
        LocalDate date,

        @Schema(description = "Lista de ítems del menú seleccionados")
        List<MenuTypeEntity> items
) {}