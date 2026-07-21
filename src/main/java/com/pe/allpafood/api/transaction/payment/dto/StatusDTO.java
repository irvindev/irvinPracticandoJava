package com.pe.allpafood.api.transaction.payment.dto;

public record StatusDTO(
        boolean success,
        String message
) {
}
