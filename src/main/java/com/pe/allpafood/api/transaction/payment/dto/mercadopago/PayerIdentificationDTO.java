package com.pe.allpafood.api.transaction.payment.dto.mercadopago;

import jakarta.validation.constraints.NotNull;

public record PayerIdentificationDTO(
    @NotNull
     String type,
    @NotNull
     String number

) {
}
