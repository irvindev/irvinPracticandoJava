package com.pe.allpafood.api.gateway.admin.plans.dto;

import com.pe.allpafood.api.transaction.user.entities.ProfileEntity;
import com.pe.allpafood.api.transaction.user.entities.RoleEntity;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;
import java.time.LocalDateTime;



@Getter
@Setter
@ToString
public class UserDTO {
    private String id;
    private String phoneNumber;
    private String email;
    private String documentNumber;
    private Integer status;
    private ProfileDTO profile;
    
    private LocalDate registerDate;
}
