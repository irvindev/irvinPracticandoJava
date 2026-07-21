package com.pe.allpafood.api.transaction.order.entity;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderSummary {
    private Integer orderId;
    private Integer menuId;
    private Integer menuTypeId;
    private String menuType;
    private String menuName;
    private String userId;
    private boolean corporateUser;
}
