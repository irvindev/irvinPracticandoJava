package com.pe.allpafood.api.transaction.payment.entities;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class VoucherEntity {
    private String invoiceId;
    private String imageUrl;
    private String paymentMethod;
    private String phoneNumber;
    private LocalDateTime sendDate;
}
