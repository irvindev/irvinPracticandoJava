package com.pe.allpafood.api.gateway.order_admin.dto;

import java.util.List;

public record AssignDeliveryPoint (
        String userId,
        List<Long> orderIds
){
}
