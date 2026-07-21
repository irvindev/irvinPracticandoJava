package com.pe.allpafood.api.transaction.plan.dto;

import com.pe.allpafood.api.transaction.user.dto.DeliveryDTO;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record UserSubscriptionDTO (
        @NotNull
        Integer planId,
        List<String> additional,
        List<Integer> complementsId,
        @NotNull
        String paymentMethodType,
        @NotNull
        String paymentMethodId,
        @NotNull
        String paymentToken,
        DeliveryDTO  invoiceAddress
){
}
