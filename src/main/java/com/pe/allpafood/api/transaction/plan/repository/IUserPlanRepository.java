package com.pe.allpafood.api.transaction.plan.repository;

import com.pe.allpafood.api.core.utils.dto.PageResult;
import com.pe.allpafood.api.gateway.admin.plans.dto.UserPlanDTO;
import com.pe.allpafood.api.transaction.plan.entities.ObjectiveMetric;
import com.pe.allpafood.api.transaction.plan.entities.UserPlanEntity;
import com.pe.allpafood.api.transaction.plan.entities.benefits.ConsumeBenefits;

import java.time.LocalDate;
import java.util.List;

public interface IUserPlanRepository {
    PageResult<UserPlanDTO> findUsersWithPlan(String search, int page, int size);
    UserPlanEntity findByUserId(String userId);

    UserPlanEntity findBenefitsConsumptionByUserId(String userId);

    List<UserPlanEntity> findAllDataByMajorDate(LocalDate date);

    List<UserPlanEntity>  findAllDataByRangeDate(LocalDate minor, LocalDate major);

    List<UserPlanEntity> findAllDataByDate(LocalDate date);

    List<ObjectiveMetric> getObjectiveRegistrationByUserId(String userId);
    void updateObjectiveMetrics(List<ObjectiveMetric> objectiveMetrics, String userId);
    void updateBenefitsConsumption(String userId, ConsumeBenefits consumption);

    void updateBenefitsAndCredits(UserPlanEntity userPlan);

    void saveUserPlanRepository(String userId, UserPlanEntity userPlan);
    Integer findBenefitsIdByUserIAndExpirationDate(String userId, LocalDate date);
    boolean existByUserIdAndExpDate(String userId, LocalDate today);
    void updateNeedDay(UserPlanEntity userPlan);
    String findNeedDayByUserId(String userId);
    List<UserPlanEntity> getAllUserPlansAvailableByDate(LocalDate today);

    void updatePlanUser(UserPlanEntity entity);
}
