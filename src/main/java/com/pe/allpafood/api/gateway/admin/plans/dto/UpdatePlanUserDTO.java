package com.pe.allpafood.api.gateway.admin.plans.dto;

import com.pe.allpafood.api.transaction.plan.entities.benefits.ConsumeBenefits;

import java.time.LocalDate;

public record UpdatePlanUserDTO (
        LocalDate planInitDate,
        LocalDate planExpirationDate,
        ConsumeBenefits consumedBenefits,
        ConsumeBenefits credits,
        Integer benefitId
){
}
