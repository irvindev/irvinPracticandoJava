package com.pe.allpafood.api.transaction.order.entity;

import com.pe.allpafood.api.transaction.user.entities.ProfileEntity;
import lombok.Data;


@Data
public class OrderUserEntity {
    ProfileEntity userProfile;
    OrderEntity orderEntity;
}
