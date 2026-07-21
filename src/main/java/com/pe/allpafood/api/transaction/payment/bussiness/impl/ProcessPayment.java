package com.pe.allpafood.api.transaction.payment.bussiness.impl;

import com.pe.allpafood.api.transaction.payment.dto.PaymentResult;
import com.pe.allpafood.api.transaction.payment.dto.generic.GenericPaymentDTO;
import com.pe.allpafood.api.transaction.payment.processors.IPaymentProcessor;
import com.pe.allpafood.api.transaction.payment.processors.ProcessorFactory;
import org.springframework.stereotype.Service;

@Service
public class ProcessPayment {
    private final ProcessorFactory processorFactory;

    public ProcessPayment(ProcessorFactory processorFactory) {
        this.processorFactory = processorFactory;
    }

    public PaymentResult process(GenericPaymentDTO request) {
        IPaymentProcessor processor = processorFactory.getProcessor(request.processor());
        return processor.execute(request);
    }
}
