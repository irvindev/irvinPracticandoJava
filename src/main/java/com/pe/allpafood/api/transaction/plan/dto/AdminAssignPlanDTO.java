package com.pe.allpafood.api.transaction.plan.dto;

import jakarta.validation.constraints.NotNull;
import java.util.List;

public record AdminAssignPlanDTO(

        @NotNull(message = "El userId es obligatorio.")
        String userId,

        @NotNull(message = "El planId es obligatorio.")
        Integer planId,

        List<Integer> complements
) {
}