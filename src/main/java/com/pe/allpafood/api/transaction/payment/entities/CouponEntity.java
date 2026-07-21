package com.pe.allpafood.api.transaction.payment.entities;

import lombok.Data;

@Data
public class CouponEntity {

    private String id;
    private String name;
    private Integer value;
    private String type;
}
