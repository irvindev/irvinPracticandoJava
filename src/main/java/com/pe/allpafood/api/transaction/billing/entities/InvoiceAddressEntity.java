package com.pe.allpafood.api.transaction.billing.entities;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Data
public class InvoiceAddressEntity {
    private Integer id;
    private String userId;
    private String address;
    private String description;
    private boolean active;
}

