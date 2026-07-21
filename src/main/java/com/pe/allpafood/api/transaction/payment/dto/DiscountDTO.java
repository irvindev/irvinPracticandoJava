package com.pe.allpafood.api.transaction.payment.dto;

public record DiscountDTO (
        Float initialAmount,
        Float finalAmount,
        Float discountedAmount

){
}
