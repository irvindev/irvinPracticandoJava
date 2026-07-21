package com.pe.allpafood.api.transaction.payment.processors.impl.mercadopago;

import com.pe.allpafood.api.transaction.payment.processors.IPaymentProcessor;

public interface IMercadoPagoMethodProcessor extends IPaymentProcessor {
    String getMethodName(); // "yape", "tarjeta", etc.
}
