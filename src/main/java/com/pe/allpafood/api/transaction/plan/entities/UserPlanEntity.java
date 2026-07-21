package com.pe.allpafood.api.transaction.plan.entities;

import com.pe.allpafood.api.transaction.plan.entities.benefits.ConsumeBenefits;

import com.pe.allpafood.api.transaction.user.entities.UserEntity;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class UserPlanEntity {
    private String userId;
    private UserEntity user;
    private Integer benefitsId;
    private Integer recommendedPlanId;
    private LocalDate planInitDate;
    private LocalDate planExpirationDate;
    private ConsumeBenefits consumedBenefits;
    private String consumedBenefitsJson;
    private ConsumeBenefits credits;
    private String creditsJson;
    private String needDay;
    private List<ObjectiveMetric> objectivesRegistration;
    private List<SummaryWeek> summaryWeek;
    private String modifiedBy;
    private LocalDateTime modifiedAt;
}
