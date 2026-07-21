package com.pe.allpafood.api.gateway.admin.plans.dto;


import com.pe.allpafood.api.transaction.plan.entities.benefits.ConsumeBenefits;
import lombok.Data;

import java.time.LocalDate;

import java.math.BigDecimal;


@Data
public class UserPlanDTO {
    String userId;
    UserDTO user;
    Integer benefitsId;
    BenefitsDTO benefits;
    LocalDate planInitDate;
    LocalDate planExpirationDate;
    ConsumeBenefits consumedBenefits;
    ConsumeBenefits credits;

    private String paymentMethod;
    private BigDecimal totalPrice;
}
