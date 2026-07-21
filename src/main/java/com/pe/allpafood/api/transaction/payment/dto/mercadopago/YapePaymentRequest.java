package com.pe.allpafood.api.transaction.payment.dto.mercadopago;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class YapePaymentRequest {

    private String token;

    @JsonProperty("transaction_amount")
    private BigDecimal transactionAmount;

    private String description;

    private Integer installments;

    @JsonProperty("payment_method_id")
    private String paymentMethodId;

    private Payer payer;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Payer {
        private String email;
    }
}
