package com.pe.allpafood.api.gateway.admin.plans.dto;

import com.pe.allpafood.api.transaction.user.entities.InformationEntity;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;
import java.time.LocalDateTime;
@Getter
@Setter
@ToString
public class ProfileDTO {
    private String userId;
    private String name;
    private String lastname;
    private LocalDate bornDate;
    private String district;
    private String address;
    private String location;
    private InformationEntity informationDeserializer;
    private String information;
    private Integer recommendedPlanId;
    private Boolean planActive;
    private String phoneNumber;
    private LocalDate registerDate;
}