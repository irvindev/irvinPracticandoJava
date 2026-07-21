package com.pe.allpafood.api.transaction.payment.dto;

import jakarta.validation.constraints.NotNull;

public record CouponInDTO (
        @NotNull
        String code,
        @NotNull
        Float amount
){

}
