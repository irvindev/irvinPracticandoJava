package com.pe.allpafood.api.transaction.payment.entities.izipay;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class TransactionEntity {
    private String uuid;
}