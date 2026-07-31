package com.pe.allpafood.api.gateway.admin.plans.dto;

import com.pe.allpafood.api.transaction.plan.entities.benefits.DetailEntity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;
import java.util.List;

public record UpdateSubscriptionPlanDTO(
        @NotBlank(message = "Descripción no puede estar vacía")
        String description,
        @NotNull(message = "Precio real requerido")
        Float realPrice,
        Float previousPrice,
        String level,
        List<DetailEntity<Float>> properties,
        List<String> descriptionList,
        List<String> extraBenefits,
        List<String> principalBenefits
) implements Serializable {
}