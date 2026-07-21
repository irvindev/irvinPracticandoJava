package com.pe.allpafood.api.transaction.catalog.dto;

import com.pe.allpafood.api.transaction.plan.entities.benefits.DetailEntity;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record MenuInfoDTO (
        Integer id,
        String name,
        String description,
        List<DetailEntity<Float>> properties


) {
}