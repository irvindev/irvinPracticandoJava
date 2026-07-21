package com.pe.allpafood.api.transaction.billing.dto;

import com.pe.allpafood.api.transaction.plan.entities.benefits.DetailEntity;
import com.pe.allpafood.api.transaction.billing.entities.InvoiceAddressEntity;

import java.time.LocalDate;
import java.util.List;

public record InvoiceDTO(
        Long id,
        String status,
        LocalDate emissionDate,
        Float totalPrice,
        List<DetailEntity<Float>> details,
        InvoiceAddressEntity invoiceAddress
) {
}
