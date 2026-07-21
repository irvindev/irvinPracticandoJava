package com.pe.allpafood.api.transaction.payment.processors.impl.mercadopago;


import com.mercadopago.MercadoPagoConfig;
import com.mercadopago.client.common.IdentificationRequest;
import com.mercadopago.client.payment.*;

import com.mercadopago.exceptions.MPApiException;
import com.mercadopago.exceptions.MPException;
import com.mercadopago.resources.payment.Payment;
import com.pe.allpafood.api.core.exception.BusinessException;
import com.pe.allpafood.api.transaction.payment.dto.generic.GenericPaymentDTO;
import com.pe.allpafood.api.transaction.payment.dto.mercadopago.*;
import com.pe.allpafood.api.transaction.payment.dto.PaymentResult;
import com.pe.allpafood.api.transaction.payment.dto.generic.GetTokenDTO;
import com.pe.allpafood.api.transaction.payment.processors.BasePaymentProcessor;
import com.pe.allpafood.api.transaction.payment.processors.ISupportsFormToken;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class MercadoPagoYapeProcessor extends BasePaymentProcessor implements IMercadoPagoMethodProcessor, ISupportsFormToken {

    @Value("${payment.mercado-pago.public-endpoint.platform-payments}")
    private String paymentTokensEndpoint;

    private final UtilsPayment utilsPayment;

    private final RestTemplate rest;

    @Override
    public String getMethodName() {
        return "yape";
    }

    @Override
    public boolean isAsync() {
        return true;
    }

    @Override
    public Optional<String> generateFormToken(GetTokenDTO request) {
        log.info("[generateFormToken] Starting ");
        YapeTokenDTO paymentDTO = (YapeTokenDTO) request;
        log.debug("[generateFormToken] resp {}",paymentDTO);
        Map<String, Object> body = Map.of(
                "phoneNumber", paymentDTO.getPhone(),
                "otp", paymentDTO.getOtp()
        );

        HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String,Object>> entity = new HttpEntity<>(body, headers);

        ResponseEntity<Map> resp = rest.postForEntity(paymentTokensEndpoint, entity, Map.class);
        log.debug("[generateFormToken] resp {}",resp);
        if (resp.getStatusCode().is2xxSuccessful()) {
            String token = (String)(resp.getBody().get("id"));
            return Optional.of(token);
        }

        return Optional.empty();
    }


    @Override
    public PaymentResult<Payment> execute(GenericPaymentDTO request) {
        return utilsPayment.payment(request);
    }
}