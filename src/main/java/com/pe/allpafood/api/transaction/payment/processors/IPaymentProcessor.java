package com.pe.allpafood.api.transaction.payment.processors;


import com.pe.allpafood.api.transaction.payment.dto.generic.GenericPaymentDTO;
import com.pe.allpafood.api.transaction.payment.dto.PaymentResult;

public interface IPaymentProcessor {
    PaymentResult execute(GenericPaymentDTO request);
    default boolean isAsync() {
        return false;
    }
}
