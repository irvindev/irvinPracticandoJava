package com.pe.allpafood.api.transaction.payment.dto.mercadopago;

public record ProductDTO (
        String id,
        String name,
        String description,
        Integer quantity,
        Float unitPrice
){
}
