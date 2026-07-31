package com.pe.allpafood.api.transaction.plan.bussiness;

import com.pe.allpafood.api.gateway.admin.plans.dto.UpdateSubscriptionPlanDTO;
import com.pe.allpafood.api.transaction.plan.dto.SubscriptionPlanDTO;
import com.pe.allpafood.api.transaction.plan.entities.benefits.SubscriptionPlanEntity;

import java.util.List;
import java.util.Map;

public interface IPlanService {

    List<SubscriptionPlanDTO> getAll();
    Map<String,Object> getPlanRecommended(String userId);
    SubscriptionPlanEntity getPlanPrices(int planId);

    List<SubscriptionPlanDTO> getAllForAdmin();
    void updatePlan(Integer planId, UpdateSubscriptionPlanDTO dto);
}
