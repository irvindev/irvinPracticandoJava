package com.pe.allpafood.api.transaction.plan.bussiness.impl;

import com.pe.allpafood.api.transaction.plan.dto.SubscriptionPlanDTO;
import com.pe.allpafood.api.transaction.plan.entities.benefits.SubscriptionPlanEntity;
import com.pe.allpafood.api.transaction.plan.repository.impl.SubscriptionPlanRepository;
import com.pe.allpafood.api.transaction.plan.bussiness.IPlanService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class PlanService implements IPlanService {

    private final SubscriptionPlanRepository subscriptionPlanRepository;

    @Override
    public List<SubscriptionPlanDTO> getAll() {
        List<SubscriptionPlanEntity> entityList = subscriptionPlanRepository.findAvailablePlans();
        return mapToDTO(entityList);
    }

    @Override
    public Map<String,Object> getPlanRecommended(String userId){
        List<SubscriptionPlanEntity> entityList = subscriptionPlanRepository.findAvailablePlans();
        Integer recommendedPlanId = subscriptionPlanRepository.findRecommendedPlanIdByUserId(userId);

        Map<String, Object> response =  new HashMap<>();
        response.put("recommendedPlanId", recommendedPlanId);
        response.put("plans", mapToDTO(entityList));
        return response;
    }

    @Override
    public SubscriptionPlanEntity getPlanPrices(int planId) {
        return subscriptionPlanRepository.findPriceByPlanId(planId);
    }

    private List<SubscriptionPlanDTO> mapToDTO(List<SubscriptionPlanEntity> entities){
        List<SubscriptionPlanDTO> subscriptionPlanDTOList = new ArrayList<>();
        for (SubscriptionPlanEntity entity:entities){
            subscriptionPlanDTOList.add(new SubscriptionPlanDTO(
                    entity.getId(),
                    entity.getDescription(),
                    entity.getRealPrice(),
                    entity.getPreviousPrice(),
                    entity.getLevel(),
                    entity.getBenefits(),
                    entity.getPropertiesEntity()
            ));
        }
        return subscriptionPlanDTOList;
    }
}
