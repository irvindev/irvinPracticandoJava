package com.pe.allpafood.api.transaction.plan.bussiness.impl;

import com.pe.allpafood.api.core.utils.converter.JsonUtil;
import com.pe.allpafood.api.core.utils.dto.PageResult;
import com.pe.allpafood.api.gateway.admin.plans.dto.UpdatePlanUserDTO;
import com.pe.allpafood.api.gateway.admin.plans.dto.UserPlanDTO;
import com.pe.allpafood.api.transaction.plan.entities.UserPlanEntity;
import com.pe.allpafood.api.transaction.plan.entities.benefits.ConsumeBenefits;
import com.pe.allpafood.api.transaction.plan.repository.IUserPlanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class UserPlanService {

    private final IUserPlanRepository userPlanRepository;

    public void changeNeedDayInformation(String userId, String needDay){
        UserPlanEntity userPlan = new UserPlanEntity();
        userPlan.setUserId(userId);
        userPlan.setNeedDay(needDay);
        userPlanRepository.updateNeedDay(userPlan);
    }

    public String getNeedDay(String userId) {
        return userPlanRepository.findNeedDayByUserId(userId);
    }

    public boolean isUserPlanAvailable(String userId) {
        return userPlanRepository.existByUserIdAndExpDate(userId, LocalDate.now());
    }

    public UserPlanEntity getUserPlan(String userId) {
        return userPlanRepository.findBenefitsConsumptionByUserId(userId);
    }

    public PageResult<UserPlanDTO> getUserPlansPaginated(String search, int page, int size) {
        return userPlanRepository.findUsersWithPlan(search, page, size);

    }

    public void updateUserPlan(String userPlanId, UpdatePlanUserDTO userPlanDTO, String userId) {
        UserPlanEntity userPlan = new UserPlanEntity();
        userPlan.setUserId(userPlanId);
        userPlan.setModifiedBy(userId);
        userPlan.setPlanExpirationDate(userPlanDTO.planExpirationDate());
        userPlan.setPlanInitDate(userPlanDTO.planInitDate());
        userPlan.setBenefitsId(userPlanDTO.benefitId());
        if(userPlanDTO.credits() != null) userPlan.setCreditsJson(JsonUtil.convertToJsonString(userPlanDTO.credits()));
        if(userPlanDTO.consumedBenefits() != null) userPlan.setConsumedBenefitsJson(JsonUtil.convertToJsonString(userPlanDTO.consumedBenefits()));
        userPlanRepository.updatePlanUser(userPlan);
    }

}
