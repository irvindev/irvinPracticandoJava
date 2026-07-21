package com.pe.allpafood.api.transaction.payment.bussiness.impl.old;

import com.pe.allpafood.api.core.exception.BusinessException;

import com.pe.allpafood.api.core.utils.converter.JsonUtil;
import com.pe.allpafood.api.core.utils.converter.TimeUtil;
import com.pe.allpafood.api.transaction.payment.dto.FormDataDTO;
import com.pe.allpafood.api.transaction.payment.dto.TokenDTO;
import com.pe.allpafood.api.transaction.payment.dto.PaymentAnswerDTO;
import com.pe.allpafood.api.transaction.payment.dto.StatusDTO;
import com.pe.allpafood.api.transaction.payment.entities.PaySimpleEntity;
import com.pe.allpafood.api.transaction.payment.entities.izipay.ChargedEntity;
import com.pe.allpafood.api.transaction.payment.entities.izipay.CustomerEntity;
import com.pe.allpafood.api.transaction.payment.entities.izipay.IpnEntity;
import com.pe.allpafood.api.transaction.payment.entities.izipay.PaymentEntity;
import com.pe.allpafood.api.transaction.payment.bussiness.impl.izipay.IziPayProcessor;
import com.pe.allpafood.api.transaction.plan.repository.ISubscriptionPlanRepository;
import com.pe.allpafood.api.transaction.payment.repository.PaymentRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;



@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionService {
    private final ISubscriptionPlanRepository subscriptionPlanRepository;
    private final IziPayProcessor iziPayProcessor;
    private final PaymentRepository paymentRepository;
    @Value("${payment.izi-pay.public-key}")
    private String publicKey;

    @Transactional
    public TokenDTO registerPayment(FormDataDTO dto) {

        Float planPriceFloat = subscriptionPlanRepository.findRealPriceByPlanId(dto.planId());

        if (planPriceFloat == null) throw new BusinessException("El plan seleccionado no existe.");

        PaymentEntity payment = new PaymentEntity();
        CustomerEntity customerEntity = new CustomerEntity();
        customerEntity.setEmail(dto.customer().email());
        payment.setCustomer(customerEntity);

        var amount = getAmount(planPriceFloat);
        payment.setAmount(amount);
        payment.setCurrency("PEN");

        long orderId = paymentRepository.insertSimplePayment(mapPaySimple(dto,amount));
        payment.setOrderId(String.valueOf(orderId));
        try{
            ChargedEntity chargedEntity = iziPayProcessor.chargePayment(payment);
            log.debug("[registerPayment] chargeEntity {}" , chargedEntity);
            if (!chargedEntity.getStatus().equals("SUCCESS")) {
                log.error("[registerPayment] chargedEntity error {} ", chargedEntity.getAnswer().getErrorMessage());
                throw new BusinessException("No se pudo realizar el pago.");
            }

            String formToken = chargedEntity.getAnswer().getFormToken();
            return new TokenDTO(chargedEntity.getStatus(),formToken, publicKey);
        }catch (Exception e){
            throw new BusinessException(e.getMessage());
        }
    }

    @Transactional
    public StatusDTO changeStatusPayment(PaymentAnswerDTO dto, String password) {
        log.debug("[changeStatusPayment] Starting: {} ", dto);

        if (!checkHash(dto, password)) {
            log.error("[changeStatusPayment] IPN Response not valid hash: {} ", dto);
            throw  new BusinessException("No valid hash IPN");
        }

        log.debug("[changeStatusPayment] Starting map json: {} ", dto.krAnswer());
        IpnEntity ipnResponse = JsonUtil.convertToObject(dto.krAnswer(), IpnEntity.class);

        if (ipnResponse == null){
            log.error("[changeStatusPayment] IPN Response not valid json: {} ", dto.krAnswer());
            throw  new BusinessException("No valid response IPN");
        }

        String orderStatus = ipnResponse.getOrderStatus();
        boolean success = "PAID".equals(orderStatus);
        String messageResponse = success ? "ok" : "Pago no completado. Estado: "+ ipnResponse.getOrderStatus();

        try{
            long orderId = Long.parseLong(ipnResponse.getOrderDetails().getOrderId());
            log.info("[changeStatusPayment] order id : {} ", orderId);
            String uuid = ipnResponse.getTransactions().get(0).getUuid();
            paymentRepository.updateStatusPayment("C",orderStatus,messageResponse,uuid, TimeUtil.getPeruDateTime(),orderId);
            log.info("[changeStatusPayment] return response message: {} ", messageResponse);
            return new StatusDTO(success,messageResponse);
        }catch (NumberFormatException e){
            log.error("[changeStatusPayment] OrderId not valid: {} ", ipnResponse.getOrderDetails().getOrderId());
            throw new BusinessException("Orden no valida.");
        }
    }

    public boolean checkHash(PaymentAnswerDTO dto, String key){
        log.debug("[checkHash] Starting: {} - {}", dto, key);
        log.debug("[checkHash] krHash: {}", dto.krHash());
        String calculatedHash = iziPayProcessor.decodeHmacSha256(dto.krAnswer(), key);
        log.debug("[checkHash] calculatedHash: {} ", calculatedHash);
        return calculatedHash.equals(dto.krHash());
    }

    private int getAmount(Float planPriceFloat){
        BigDecimal planPrice = new BigDecimal(planPriceFloat);
        return planPrice.setScale(2, RoundingMode.HALF_UP).multiply(new BigDecimal(100)).intValue();
    }

    private PaySimpleEntity mapPaySimple(FormDataDTO dto, float amount){
        PaySimpleEntity paySimple= new PaySimpleEntity();
        paySimple.setAmount(amount);
        paySimple.setName(dto.customer().name());
        paySimple.setPlanId(dto.planId());
        paySimple.setCreationDate(TimeUtil.getPeruDateTime());
        paySimple.setEmail(dto.customer().email());
        paySimple.setLastname(dto.customer().lastname());
        paySimple.setPhoneNumber(dto.customer().phoneNumber());
        paySimple.setAddress(dto.customer().address());
        paySimple.setDistrict(dto.customer().district());
        paySimple.setStatus("P");
        return paySimple;
    }
}
