package com.pe.allpafood.api.transaction.plan.entities.benefits;

import lombok.Data;

import java.util.List;

@Data
public class BenefitsEntity {
    private Integer id;
    private Integer benefitsPeriod;
    private List<DetailEntity<Integer>> detail;
    private Integer consumptionTotal;
    private Boolean assignedPlan;
    private Integer subscriptionPlanId;
    private SubscriptionPlanEntity subscriptionPlan;
    private List<String> extraBenefits;
    private List<String> principalBenefits;
}
