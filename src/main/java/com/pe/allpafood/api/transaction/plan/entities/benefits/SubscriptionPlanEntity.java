package com.pe.allpafood.api.transaction.plan.entities.benefits;


import lombok.Data;

import java.util.List;

@Data
public class SubscriptionPlanEntity {
    private int id;
    private String description;
    private Float realPrice;
    private Double previousPrice;
    private String level;
    private boolean available;
    private BenefitsEntity benefits;
    private boolean recommended;
    private Float discountAmount;
    private Integer discountPercent;
    
    private List<DetailEntity<Float>> propertiesEntity;
    private String properties;

    private List<String> descriptionListEntity;
    private String descriptionList;            
}
