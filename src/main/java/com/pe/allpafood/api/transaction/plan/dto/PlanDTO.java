package com.pe.allpafood.api.transaction.plan.dto;

import com.pe.allpafood.api.transaction.plan.entities.benefits.ConsumeBenefits;

import java.time.LocalDate;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Datos del plan de alimentación del usuario")
public record PlanDTO(

        @Schema(description = "ID del plan", example = "101")
        Integer id,

        @Schema(description = "Nombre del plan de alimentación", example = "Plan Saludable Básico")
        String planName,

        @Schema(description = "Beneficios de consumo del plan")
        ConsumeBenefits consumption,

        @Schema(description = "Beneficios de consumo al acabar el plan actual")
        ConsumeBenefits credits,

        @Schema(description = "Fecha de expiración del plan", example = "2025-06-01")
        LocalDate expirationDate,

        @Schema(description = "Fecha de inicio del plan", example = "2025-05-01")
        LocalDate initDate
) {}
