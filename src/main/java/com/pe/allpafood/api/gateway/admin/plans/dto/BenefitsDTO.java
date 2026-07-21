package com.pe.allpafood.api.gateway.admin.plans.dto;

import com.pe.allpafood.api.transaction.plan.entities.benefits.SubscriptionPlanEntity;
import lombok.Data;


@Data
public class BenefitsDTO {
    private Integer id;
    private SubscriptionPlanDTO subscriptionPlan;

}

