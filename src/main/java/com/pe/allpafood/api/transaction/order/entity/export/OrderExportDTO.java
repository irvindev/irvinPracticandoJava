package com.pe.allpafood.api.transaction.order.dto.export;

import lombok.Data;

@Data
public class OrderExportDTO {
    private Long orderId;
    private String clientName;
    private String clientLastname;
    private String motorizedName;
    private String motorizedLastname;
    private String district;
    private String sugar;                 // "Sí" / "No"
    private String alimentsRestrictions;
    private String doubleProtein;          // "Sí" / "No"
    private String snack;                  // "Sí" / "No"
    private String status;
}