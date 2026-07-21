package com.pe.allpafood.api.transaction.payment.entities;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class PaySimpleEntity {

    private long id;
    private String name;
    private String lastname;
    private String email;
    private String phoneNumber;
    private String status;
    private float amount;
    private int planId;
    private LocalDateTime creationDate;
    private LocalDateTime operationDate;
    private String address;
    private String district;
    private String orderStatus;
    private String messageOperation;
    private String transactionId;
}
