package com.pe.allpafood.api.transaction.payment.dto;

public record TokenDTO(
        String message,
        String token,
        String publicKey
) {
}
