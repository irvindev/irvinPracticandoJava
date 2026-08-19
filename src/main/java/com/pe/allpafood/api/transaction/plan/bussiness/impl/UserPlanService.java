package com.pe.allpafood.api.transaction.plan.bussiness.impl;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pe.allpafood.api.core.exception.BusinessException;
import com.pe.allpafood.api.core.utils.converter.JsonUtil;
import com.pe.allpafood.api.core.utils.dto.PageResult;
import com.pe.allpafood.api.gateway.admin.plans.dto.UpdatePlanUserDTO;
import com.pe.allpafood.api.gateway.admin.plans.dto.UserPlanDTO;
import com.pe.allpafood.api.transaction.plan.entities.UserPlanEntity;
import com.pe.allpafood.api.transaction.plan.entities.benefits.BenefitsEntity;
import com.pe.allpafood.api.transaction.plan.repository.IUserPlanRepository;
import com.pe.allpafood.api.transaction.plan.repository.impl.SubscriptionPlanRepository;
import com.pe.allpafood.api.transaction.user.entities.UserEntity;
import com.pe.allpafood.api.transaction.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserPlanService {

    private final IUserPlanRepository userPlanRepository;
    private final UserRepository userRepository;
    private final SubscriptionPlanRepository subscriptionPlanRepository;
    private final SubscriptionService subscriptionService;

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

    @Transactional(rollbackFor = {BusinessException.class, Exception.class})
    public void assignPlanByAdmin(String userId, Integer planId, String paymentMethodType, String paymentMethodId) throws BusinessException {

        UserEntity user = userRepository.findById(userId);
        if (user == null) {
            throw new BusinessException("Usuario no encontrado.");
        }

        BenefitsEntity benefitsEntity = subscriptionPlanRepository.findBenefitsByPlanId(planId);
        if (benefitsEntity == null) {
            throw new BusinessException("El plan seleccionado no existe.");
        }

        subscriptionService.subscribeUserToPlan(userId, benefitsEntity, new ArrayList<>(), null, null);

        log.info("[assignPlanByAdmin] Plan {} asignado exitosamente a usuario {} (paymentMethodType={}, paymentMethodId={} — no persistido en factura por falta de dirección de facturación)",
                planId, userId, paymentMethodType, paymentMethodId);
    }

}