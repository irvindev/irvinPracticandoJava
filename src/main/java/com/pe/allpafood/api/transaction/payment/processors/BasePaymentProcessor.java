package com.pe.allpafood.api.transaction.payment.processors;

import com.pe.allpafood.api.transaction.payment.dto.generic.GenericPaymentDTO;
import com.pe.allpafood.api.transaction.payment.dto.PaymentResult;

public abstract class BasePaymentProcessor implements IPaymentProcessor {

    @Override
    public PaymentResult execute(GenericPaymentDTO request) {
        try {
            validate(request);
            authorize(request);
            capture(request);
            return PaymentResult.success("Procesado exitosamente");
        } catch (Exception e) {
            return PaymentResult.failure("Error: " + e.getMessage());
        }
    }

    protected void validate(GenericPaymentDTO request) throws Exception {}
    protected void authorize(GenericPaymentDTO request) throws Exception {}
    protected void capture(GenericPaymentDTO request) throws Exception {}
}
