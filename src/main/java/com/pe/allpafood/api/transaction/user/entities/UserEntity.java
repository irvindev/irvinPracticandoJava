package com.pe.allpafood.api.transaction.user.entities;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.Set;

@Getter
@Setter
@ToString
public class UserEntity {
    private String id;
    private String password;
    private boolean registrationCompleted;
    private boolean profileCompleted;
    private String phoneNumber;
    private Boolean verified;
    private Set<RoleEntity> roles;
    private String verificationCode;
    private String provider;
    private String email;
    private String documentNumber;
    private LocalDateTime verificationCodeExp;
    private boolean corporateUser;
    private Integer corporationId;
    private Integer status;
    private ProfileEntity profile;

}
