package com.pe.allpafood.api.transaction.order.entity;

import com.pe.allpafood.api.core.utils.dto.GeoPoint;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Data
public class DeliveryPointEntity {
    private Long id;
    private String userId;
    private String address;
    private String description;
    private String location;
    private boolean assigned;
    private GeoPoint geoLocation;
    private boolean active;
    private String district;
}
