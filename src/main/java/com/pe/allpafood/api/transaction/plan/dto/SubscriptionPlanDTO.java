package com.pe.allpafood.api.transaction.plan.dto;

import com.pe.allpafood.api.transaction.plan.entities.benefits.BenefitsEntity;
import com.pe.allpafood.api.transaction.plan.entities.benefits.DetailEntity;

import java.util.List;

public record SubscriptionPlanDTO(
        Integer id,
        String description,
        Float price,
        Double previousPrice,
        String level,
        BenefitsEntity benefits,
        List<DetailEntity<Float>> properties
) {

}
