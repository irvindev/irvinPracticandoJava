package com.pe.allpafood.api.transaction.user.entities;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@ToString
public class ProfileEntity {
    private String userId;
    private String name;
    private String lastname;
    private LocalDate bornDate;
    private String district;
    private String address;
    private String location;
    private InformationEntity informationDeserializer;
    private String information;
    private String imageUrl;
    private String documentNumber;
    private String email;
    private Integer recommendedPlanId;
    private Boolean planActive;
    private String phoneNumber;
    private String modifiedBy;
    private LocalDateTime modifiedAt;
    private LocalDate registerDate;
}
