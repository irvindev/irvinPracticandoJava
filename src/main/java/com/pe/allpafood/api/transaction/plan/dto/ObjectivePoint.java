package com.pe.allpafood.api.transaction.plan.dto;

import jakarta.validation.constraints.NotNull;

public record ObjectivePoint (
        @NotNull(message = "El peso es requerido.")
        Float weight
){
}
