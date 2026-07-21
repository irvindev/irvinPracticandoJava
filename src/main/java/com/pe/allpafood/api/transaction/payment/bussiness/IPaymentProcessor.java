package com.pe.allpafood.api.transaction.payment.bussiness;

import com.pe.allpafood.api.transaction.payment.dto.FormDataDTO;
import com.pe.allpafood.api.transaction.payment.dto.PaymentAnswerDTO;
import com.pe.allpafood.api.transaction.payment.dto.StatusDTO;
import com.pe.allpafood.api.transaction.payment.dto.TokenDTO;
import com.pe.allpafood.api.transaction.payment.entities.PaymentResponse;


public interface IPaymentProcessor {
    TokenDTO generateToken(FormDataDTO dto);
    PaymentResponse process(PaymentAnswerDTO dto);
    StatusDTO receiveIpnNotification(PaymentAnswerDTO dto);
}
