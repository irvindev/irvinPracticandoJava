package com.pe.allpafood.api.transaction.billing.entities;

import com.pe.allpafood.api.transaction.plan.entities.benefits.DetailEntity;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class InvoiceEntity {
    private Long id;
    private String description;
    private LocalDate emissionDate;
    private String status;
    private String paymentMethod;
    private Integer addressId;
    private InvoiceAddressEntity invoiceAddressEntity;
    private String details;
    private List<DetailEntity<Float>> detailsEntity;
    private String userId;
    private Float totalPrice;
    private String paymentReference;
    private String metadata;
    private String processor;
}
