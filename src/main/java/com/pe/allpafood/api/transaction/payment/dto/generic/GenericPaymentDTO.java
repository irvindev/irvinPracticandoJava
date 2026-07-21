package com.pe.allpafood.api.transaction.payment.dto.generic;


import com.pe.allpafood.api.transaction.payment.dto.mercadopago.PayerDTO;
import com.pe.allpafood.api.transaction.payment.dto.mercadopago.ProductDTO;

import java.math.BigDecimal;
import java.util.List;

public record GenericPaymentDTO (
        String method,
        String processor,
        Long orderId,
        String ip,
        String token,
        PayerDTO payer,
        String operationDescription,
        BigDecimal transactionAmount,
        Integer installments,
        List<ProductDTO> products
){
}
