package com.pe.allpafood.api.transaction.payment.dto.mercadopago;

import jakarta.validation.constraints.NotNull;

public record PayerDTO (
        String firstName,
        String lastName,
        @NotNull
        String email,

        @NotNull
        PayerIdentificationDTO identification
        ){

}
