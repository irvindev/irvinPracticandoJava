package com.pe.allpafood.api.transaction.user.bussiness.impl;

import com.pe.allpafood.api.core.exception.BusinessException;

import com.pe.allpafood.api.core.utils.converter.TimeUtil;
import com.pe.allpafood.api.transaction.order.dto.OrderDTO;
import com.pe.allpafood.api.transaction.plan.dto.FunctionalMetricsDTO;
import com.pe.allpafood.api.transaction.plan.dto.ObjectivePoint;
import com.pe.allpafood.api.transaction.plan.dto.PlanDTO;
import com.pe.allpafood.api.transaction.plan.entities.ObjectiveMetric;
import com.pe.allpafood.api.transaction.catalog.entity.MenuCalendar;
import com.pe.allpafood.api.transaction.plan.entities.benefits.SubscriptionPlanEntity;
import com.pe.allpafood.api.transaction.order.entity.OrderEntity;
import com.pe.allpafood.api.transaction.plan.entities.UserPlanEntity;
import com.pe.allpafood.api.transaction.user.bussiness.IDashboardService;
import com.pe.allpafood.api.transaction.user.entities.ProfileEntity;
import com.pe.allpafood.api.transaction.catalog.repository.IMenuRepository;
import com.pe.allpafood.api.transaction.order.repository.IOrderRepository;
import com.pe.allpafood.api.transaction.plan.repository.ISubscriptionPlanRepository;
import com.pe.allpafood.api.transaction.plan.repository.IUserPlanRepository;
import com.pe.allpafood.api.transaction.user.repository.ProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Period;
import java.time.temporal.TemporalAdjusters;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class DashboardService implements IDashboardService {

    private final IUserPlanRepository userPlanRepository;
    private final ProfileRepository profileRepository;
    private final ISubscriptionPlanRepository subscriptionPlanRepository;
    private final IMenuRepository menuRepository;
    private final IOrderRepository orderRepository;

    @Override
    public FunctionalMetricsDTO getMetrics(String userId) {
        log.info("Searching for metrics for {}", userId);
        UserPlanEntity userPlan = userPlanRepository.findByUserId(userId);

        if(userPlan == null) throw new BusinessException("El usuario aun no se ha subscrito a un plan.");

        ProfileEntity profile = profileRepository.findAgeAndHeightByUserId(userPlan.getUserId());

        log.debug("Found user plan: {}", userPlan);
        log.debug("Found profile: {}", profile);
        log.info("End searching for metrics for {}", userId);
        return new FunctionalMetricsDTO(
                userPlan.getObjectivesRegistration(),
                userPlan.getSummaryWeek(),
                Period.between(profile.getBornDate(), LocalDate.now()).getYears(),
                profile.getInformationDeserializer().getHeight()
        );
    }

    @Override
    public PlanDTO getPlan(String userId) {
        log.info("Starting get plan  for {}", userId);
        UserPlanEntity userPlan = userPlanRepository.findBenefitsConsumptionByUserId(userId);
        SubscriptionPlanEntity subscriptionPlanEntity = subscriptionPlanRepository.findSubscriptionBenefitsById(userPlan.getBenefitsId());
        return new PlanDTO(
                subscriptionPlanEntity.getId(),
                subscriptionPlanEntity.getDescription(),
                userPlan.getConsumedBenefits(),
                userPlan.getCredits(),
                userPlan.getPlanExpirationDate(),
                userPlan.getPlanInitDate()
        );
    }

    @Override
    public void registrationObjectivePoint(String userId, ObjectivePoint objectivePoint) {
        log.info("[registrationObjectivePoint] Starting... {}",userId);
        var today = LocalDate.now();
        List<ObjectiveMetric> objectiveMetrics = userPlanRepository.getObjectiveRegistrationByUserId(userId);
        Comparator<ObjectiveMetric> comparatorByDate = Comparator.comparing(ObjectiveMetric::getRegisterDate);
        objectiveMetrics.sort(comparatorByDate);

        boolean isRegisteredDate = false;
        for (ObjectiveMetric objective : objectiveMetrics){
            LocalDate dateRegistration = LocalDate.parse(objective.getRegisterDate());

            if (dateRegistration.equals(today)){
                objective.setWeight(objectivePoint.weight());
                isRegisteredDate=true;
                break;
            }
        }

        if (!isRegisteredDate){
            log.info("[registrationObjectivePoint] Already register weight today... {}",userId);
            ObjectiveMetric newMetric = new ObjectiveMetric();
            newMetric.setRegisterDate(today.toString());
            newMetric.setWeight(objectivePoint.weight());
            objectiveMetrics.add(newMetric);
        }

        userPlanRepository.updateObjectiveMetrics(objectiveMetrics,userId);
        log.info("[registrationObjectivePoint] Finishing registration {}",userId);
    }


    @Override
    public List<OrderDTO> getOrdersOfWeek(String userId) {
        LocalDate today = TimeUtil.getPeruDate();
        LocalDate startOfWeek;

        if (today.getDayOfWeek() == DayOfWeek.SATURDAY || today.getDayOfWeek() == DayOfWeek.SUNDAY) startOfWeek = today.with(TemporalAdjusters.next(DayOfWeek.MONDAY));
        else  startOfWeek = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));

        LocalDate endOfWeek = today.with(TemporalAdjusters.nextOrSame(DayOfWeek.FRIDAY));
        log.info("[getOrdersOfWeek] Starting... UserId {} startOfWeek {} endOfWeek {}",userId,startOfWeek,endOfWeek);

        List<OrderEntity> orderEntities = orderRepository.findMenusByDateRangesAndUserId(userId, startOfWeek, endOfWeek);
        log.debug("[getOrdersOfWeek] Orders: {}",orderEntities);
        List<OrderDTO> response = new ArrayList<>();

        for (OrderEntity entity : orderEntities) {
            OrderDTO orderDTO = new OrderDTO(
                    entity.getId(),
                    entity.getDeliveryDate(),
                    entity.getMenuTypes()
            );
            response.add(orderDTO);
        }
        return response;
    }

    @Override
    public List<MenuCalendar> getMenusOfWeek(String userId, LocalDate initDateWeek) {
        LocalDate nextFriday = getWeekLastDay(initDateWeek);

        log.info("[getMenusOfWeek] Starting... UserId {} startOfWeek {} endOfWeek {}",userId,initDateWeek,nextFriday);
        Integer benefitId = userPlanRepository.findBenefitsIdByUserIAndExpirationDate(userId,initDateWeek);

        if (benefitId == null) throw new BusinessException("No cuenta con beneficios de su plan en la actualidad.");

        return menuRepository.findMenuBetweenCalendarDate(userId,benefitId,initDateWeek,nextFriday);
    }

    public static LocalDate getWeekLastDay(LocalDate date) {
        if (date.getDayOfWeek() == DayOfWeek.SATURDAY || date.getDayOfWeek() == DayOfWeek.SUNDAY) {
            return date.with(TemporalAdjusters.next(DayOfWeek.FRIDAY));
        } else {
            return date.with(TemporalAdjusters.nextOrSame(DayOfWeek.FRIDAY));
        }
    }
}
