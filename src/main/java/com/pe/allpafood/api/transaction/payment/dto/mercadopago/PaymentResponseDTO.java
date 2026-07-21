package com.pe.allpafood.api.transaction.payment.dto.mercadopago;

public record PaymentResponseDTO(
        Long id,
        String status,
        String detail
) {
}
