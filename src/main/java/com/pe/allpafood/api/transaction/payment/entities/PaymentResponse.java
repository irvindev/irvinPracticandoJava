package com.pe.allpafood.api.transaction.payment.entities;

public record PaymentResponse (
        boolean success,
        String message
){
}
