package com.pe.allpafood.api.transaction.order.entity;

import com.pe.allpafood.api.transaction.catalog.entity.MenuEntity;
import com.pe.allpafood.api.transaction.catalog.entity.MenuTypeEntity;
import com.pe.allpafood.api.transaction.user.entities.ProfileEntity;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class OrderEntity {
    private String userId;
    private ProfileEntity user;
    private Long id;
    private List<Integer> items;
    private String menuTypeItems;
    private List<MenuTypeEntity> menuTypes;
    private Long deliveryPointId;
    private DeliveryPointEntity deliveryPoint;
    private LocalDateTime createDate;
    private LocalDate deliveryDate;
    private String invoiceId;
    private String status;
    private String deliveryUserId;
}
