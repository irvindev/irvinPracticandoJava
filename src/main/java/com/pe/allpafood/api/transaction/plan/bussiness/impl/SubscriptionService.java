package com.pe.allpafood.api.transaction.plan.bussiness.impl;

import com.pe.allpafood.api.core.utils.converter.TimeUtil;
import com.pe.allpafood.api.transaction.plan.entities.benefits.BenefitsEntity;
import com.pe.allpafood.api.transaction.plan.entities.benefits.ConsumeBenefits;

import com.pe.allpafood.api.transaction.plan.entities.UserPlanEntity;
import com.pe.allpafood.api.transaction.plan.repository.impl.UserPlanRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class SubscriptionService {

    private final UserPlanRepository userPlanRepository;

    public void subscribeUserToPlan(String userId, BenefitsEntity benefitsEntity, List<Integer> complements, ConsumeBenefits currentConsumedBenefits, List<String> additional) {
        log.info("Init subscribeUserToPlan con el user: {}",userId);
        UserPlanEntity userPlan = new UserPlanEntity();
        log.info("benefitsEntity is {}", benefitsEntity);

        ConsumeBenefits consumeBenefits = rebootConsumeBenefits(benefitsEntity.getConsumptionTotal(), benefitsEntity, complements, additional);
        log.debug("[subscribeUserToPlan] consumeBenefits is {}", consumeBenefits);
        LocalDate today = TimeUtil.getPeruDateTime().toLocalDate();

        if (currentConsumedBenefits == null){
            userPlan.setConsumedBenefits(consumeBenefits);
        }else{
            userPlan.setConsumedBenefits(currentConsumedBenefits);
            userPlan.setCredits(consumeBenefits);
        }

        userPlan.setBenefitsId(benefitsEntity.getId());
        userPlan.setPlanInitDate(today);
        userPlan.setPlanExpirationDate(userPlan.getPlanInitDate().plusDays(benefitsEntity.getBenefitsPeriod()));
        log.info("UserPlan is {}", userPlan);
        userPlanRepository.saveUserPlanRepository(userId,userPlan);
    }

    private ConsumeBenefits rebootConsumeBenefits(Integer total, BenefitsEntity benefitsEntity, List<Integer> complements, List<String> additional){
        ConsumeBenefits consumeBenefits = new ConsumeBenefits();

        Map<String,Integer> orders = new HashMap<>();
        orders.put("consumed",0);
        orders.put("total",total);
        consumeBenefits.setOrders(orders);

        consumeBenefits.setExtraBenefits(benefitsEntity.getExtraBenefits());
        consumeBenefits.setComplements(complements);
        consumeBenefits.setAdditional(additional);
        consumeBenefits.setPrincipalBenefits(benefitsEntity.getPrincipalBenefits());
        return consumeBenefits;
    }

}
