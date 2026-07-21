package com.pe.allpafood.api.gateway.payments;

import com.pe.allpafood.api.core.utils.dto.ErrorDTO;
import com.pe.allpafood.api.core.utils.dto.GenericMessage;
import com.pe.allpafood.api.transaction.payment.bussiness.impl.CalculateDiscount;
import com.pe.allpafood.api.transaction.payment.bussiness.IPaymentProcessor;

import com.pe.allpafood.api.transaction.payment.dto.*;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/subscriptions/payments")
@Slf4j
public class PaymentController {

    private final IPaymentProcessor paymentProcessor;
    private final CalculateDiscount calculateDiscount;

    public PaymentController(@Qualifier("iziPayProcessor") IPaymentProcessor paymentService, CalculateDiscount calculateDiscount) {
        this.paymentProcessor = paymentService;
        this.calculateDiscount = calculateDiscount;
    }


    @PostMapping("/validate-coupon")
    public DiscountDTO createPayment(@Valid @RequestBody CouponInDTO dto){
        return calculateDiscount.calculateDiscount(dto);
    }

    @PostMapping("/create")
    public TokenDTO createPayment(@Valid @RequestBody FormDataDTO dto){
        return paymentProcessor.generateToken(dto);
    }


    @PostMapping("/validate")
    public ResponseEntity<Object> processResult(@Valid @RequestBody PaymentAnswerDTO dto) {
        log.info("[processResult] processResult  request {}",dto);
        if (!paymentProcessor.process(dto).success())  {
            log.error("[processResult] error validate payment {}",dto);
            return ResponseEntity.badRequest().body(new ErrorDTO(400, "Hash invalido.", LocalDateTime.now()));
        }
        return ResponseEntity.ok(new GenericMessage("Validación correcta"));
    }

    @PostMapping(value = "/ipn", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ResponseEntity<StatusDTO> processIpn(
            @RequestParam("kr-hash") String krHash,
            @RequestParam("kr-answer") String krAnswer) {
        log.info("[processIpn] Processing IPN request");
        log.debug("[processIpn] Processing IPN request {} - {}", krHash, krAnswer);
        PaymentAnswerDTO paymentAnswerDTO = new PaymentAnswerDTO(krHash,krAnswer);
        return ResponseEntity.ok(paymentProcessor.receiveIpnNotification(paymentAnswerDTO));
    }
}
