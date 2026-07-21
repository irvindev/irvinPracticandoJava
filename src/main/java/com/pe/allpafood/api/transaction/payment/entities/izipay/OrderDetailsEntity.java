package com.pe.allpafood.api.transaction.payment.entities.izipay;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;


@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class OrderDetailsEntity {
    private String orderId;
}
