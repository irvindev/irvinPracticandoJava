package com.pe.allpafood.api.transaction.payment.bussiness.impl.izipay;

import com.pe.allpafood.api.core.exception.BusinessException;
import com.pe.allpafood.api.core.utils.file.IzipayUtil;
import com.pe.allpafood.api.core.utils.converter.JsonUtil;
import com.pe.allpafood.api.core.utils.converter.TimeUtil;
import com.pe.allpafood.api.transaction.payment.dto.*;
import com.pe.allpafood.api.transaction.payment.entities.PaySimpleEntity;
import com.pe.allpafood.api.transaction.payment.entities.izipay.ChargedEntity;
import com.pe.allpafood.api.transaction.payment.entities.izipay.CustomerEntity;
import com.pe.allpafood.api.transaction.payment.entities.izipay.IpnEntity;
import com.pe.allpafood.api.transaction.payment.entities.izipay.PaymentEntity;
import com.pe.allpafood.api.transaction.payment.repository.PaymentRepository;
import com.pe.allpafood.api.transaction.payment.bussiness.impl.CalculateDiscount;
import com.pe.allpafood.api.transaction.plan.repository.impl.SubscriptionPlanRepository;
import com.pe.allpafood.api.transaction.payment.bussiness.IPaymentProcessor;
import com.pe.allpafood.api.transaction.payment.entities.PaymentResponse;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Hex;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

@Service
@RequiredArgsConstructor
@Slf4j
public class IziPayProcessor implements IPaymentProcessor {
    private final RestTemplate restTemplate;
    private final PaymentRepository paymentRepository;
    private final SubscriptionPlanRepository subscriptionPlanRepository;
    private final CalculateDiscount calculateDiscount;

    private HttpHeaders headers;
    private String urlChargePayment;

    @Value("${payment.izi-pay.hmac-sha-256}")
    private String hmacKey;
    @Value("${payment.izi-pay.user}")
    private String user;
    @Value("${payment.izi-pay.password}")
    private String password;
    @Value("${payment.izi-pay.url.base}")
    private String urlBase;
    @Value("${payment.izi-pay.url.path.charge-payment}")
    private String pathCharge;
    @Value("${payment.izi-pay.public-key}")
    private String publicKey;

    @PostConstruct
    void init(){
        this.headers = new HttpHeaders();
        this.headers.set("Authorization", IzipayUtil.getBasicAuth(user,password));
        this.headers.set("Content-Type", "application/json");
        this.urlChargePayment = urlBase.concat(pathCharge);
    }

    @Override
    public TokenDTO generateToken(FormDataDTO dto) {
        log.info("[generateToken] Starting");
        log.debug("[generateToken] Form Data Dto:  {}",dto);
        Float planPriceFloat = subscriptionPlanRepository.findRealPriceByPlanId(dto.planId());

        if (planPriceFloat == null) throw new BusinessException("El plan seleccionado no existe.");

        PaymentEntity payment = new PaymentEntity();
        CustomerEntity customerEntity = new CustomerEntity();
        customerEntity.setEmail(dto.customer().email());
        payment.setCustomer(customerEntity);

        var amount = getAmount(planPriceFloat, dto.coupon());
        payment.setAmount(amount);
        payment.setCurrency("PEN");

        long orderId = paymentRepository.insertSimplePayment(mapPaySimple(dto,amount));
        payment.setOrderId(String.valueOf(orderId));
        try{
            ChargedEntity chargedEntity = this.chargePayment(payment);
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

    @Override
    public PaymentResponse process(PaymentAnswerDTO dto) {
        return new PaymentResponse(checkHash(dto,hmacKey),"");
    }

    @Override
    public StatusDTO receiveIpnNotification(PaymentAnswerDTO dto) {
        log.info("[changeStatusPayment] Starting ");

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

    private int getAmount(Float planPriceFloat, String coupon){

        BigDecimal finalPrice = BigDecimal.valueOf(planPriceFloat);
        if (coupon != null && !coupon.isEmpty()){
            DiscountDTO discount = calculateDiscount.calculateDiscount(new CouponInDTO(
                    coupon,
                    planPriceFloat
            ));

            finalPrice = finalPrice.subtract(BigDecimal.valueOf(discount.discountedAmount()));
        }

        return finalPrice.setScale(2, RoundingMode.HALF_UP).multiply(new BigDecimal(100)).intValue();
    }

    private boolean checkHash(PaymentAnswerDTO dto, String key){
        log.debug("[checkHash] Starting: {} - {}", dto, key);
        log.debug("[checkHash] dto hash: {} ", dto.krHash());
        String calculatedHash = decodeHmacSha256(dto.krAnswer(), key);
        log.debug("[checkHash] calculatedHash: {} ", calculatedHash);
        var response = calculatedHash.equals(dto.krHash());
        log.debug("[checkHash] valid hash: {} ", response);
        return response;
    }

    public String decodeHmacSha256(String data, String key) {
        try {
            Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
            SecretKeySpec secret_key = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            sha256_HMAC.init(secret_key);
            return Hex.encodeHexString(sha256_HMAC.doFinal(data.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException("Error generando HMAC SHA256", e);
        }
    }

    public ChargedEntity chargePayment(PaymentEntity body) {
        try{
            log.info("[sendMessageTemplate] Starting send message: {}", body);
            HttpEntity<PaymentEntity> entity = new HttpEntity<>(body,this.headers);
            var response = restTemplate.exchange(this.urlChargePayment, HttpMethod.POST, entity, ChargedEntity.class);
            log.info("[sendMessageTemplate] Terminate successful send message: {}", body);
            return response.getBody();
        } catch (RestClientException e) {
            System.err.println("[sendMessageTemplate] Error en la solicitud REST: " + e.getMessage());
            throw new BusinessException("Error al crear el pago con IziPay.");
        }
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
