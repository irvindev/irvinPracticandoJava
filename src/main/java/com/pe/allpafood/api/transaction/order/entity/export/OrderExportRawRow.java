package com.pe.allpafood.api.transaction.order.entity.export;

import lombok.Data;

@Data
public class OrderExportRawRow {
    private Long orderId;
    private String menuTypeItemsJson;
    private String status;
    private String clientName;
    private String clientLastname;
    private String motorizedName;
    private String motorizedLastname;
    private String district;
    private String clientInformationJson;
}