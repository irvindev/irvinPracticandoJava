package com.pe.allpafood.api.transaction.payment.entities.izipay;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class IpnEntity {
    private String orderStatus;
    private OrderDetailsEntity orderDetails;
    private List<TransactionEntity> transactions;
}

