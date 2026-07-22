package com.pe.allpafood.api.transaction.plan.bussiness.impl;

import com.pe.allpafood.api.transaction.plan.dto.SubscriptionPlanDTO;
import com.pe.allpafood.api.transaction.plan.entities.benefits.BenefitsEntity;
import com.pe.allpafood.api.transaction.plan.entities.benefits.SubscriptionPlanEntity;
import com.pe.allpafood.api.transaction.plan.repository.impl.SubscriptionPlanRepository;
import com.pe.allpafood.api.core.utils.converter.JsonUtil;
import com.pe.allpafood.api.gateway.admin.plans.dto.UpdateSubscriptionPlanDTO;
import com.pe.allpafood.api.transaction.plan.bussiness.IPlanService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
                    entity.getPropertiesEntity(),
                    entity.getDescriptionListEntity()
            ));
        }
        return subscriptionPlanDTOList;
    }

    @Override
    public List<SubscriptionPlanDTO> getAllForAdmin() {
        List<SubscriptionPlanEntity> entityList = subscriptionPlanRepository.findAllPlansForAdmin();
        return mapToDTO(entityList);
    }

    @Transactional
    @Override
    public void updatePlan(Integer planId, UpdateSubscriptionPlanDTO dto) {
        SubscriptionPlanEntity entity = new SubscriptionPlanEntity();
        entity.setId(planId);
        entity.setDescription(dto.description());
        entity.setRealPrice(dto.realPrice());
        entity.setPreviousPrice(dto.previousPrice() != null ? dto.previousPrice().doubleValue() : null);
        entity.setLevel(dto.level());

        if (dto.properties() != null) {
            entity.setProperties(JsonUtil.convertToJsonString(dto.properties()));
        }
        if (dto.descriptionList() != null) {
            entity.setDescriptionList(JsonUtil.convertToJsonString(dto.descriptionList()));
        }

        if (dto.extraBenefits() != null || dto.principalBenefits() != null) {
            BenefitsEntity benefitsEntity = new BenefitsEntity();
            if (dto.extraBenefits() != null) benefitsEntity.setExtraBenefitsJson(JsonUtil.convertToJsonString(dto.extraBenefits()));
            if (dto.principalBenefits() != null) benefitsEntity.setPrincipalBenefitsJson(JsonUtil.convertToJsonString(dto.principalBenefits()));
            entity.setBenefits(benefitsEntity);
        }

        subscriptionPlanRepository.updatePlanAndBenefits(entity);
    }
}
