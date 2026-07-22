package com.pe.allpafood.api.transaction.plan.repository;

import com.pe.allpafood.api.transaction.plan.entities.benefits.BenefitsEntity;
import com.pe.allpafood.api.transaction.plan.entities.benefits.SubscriptionPlanEntity;

import java.util.List;

public interface ISubscriptionPlanRepository {

    List<SubscriptionPlanEntity> findAvailablePlans();
    Integer findRecommendedPlanIdByUserId(String userId);
    SubscriptionPlanEntity findSubscriptionBenefitsById(int benefitId);
    SubscriptionPlanEntity findPriceByPlanId(int planId);
    BenefitsEntity findBenefitsByPlanId(int planId);
    Float findRealPriceByPlanId(int planId);

    List<SubscriptionPlanEntity> findAllPlansForAdmin();
    void updatePlanAndBenefits(SubscriptionPlanEntity entity);
}
