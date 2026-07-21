package com.pe.allpafood.api.transaction.catalog.dto.comanda;

import java.util.List;

public record ComandaDTO (
        List<OrderDTO> general,
        List<OrderDTO> userOrders,
        List<OrderDTO> corporateOrder,
        List<OrderDTO> complements,
        List<UserRestrictionsDTO> restrictions
){
}
