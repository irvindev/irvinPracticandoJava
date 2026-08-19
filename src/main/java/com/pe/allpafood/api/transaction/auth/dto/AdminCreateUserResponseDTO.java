package com.pe.allpafood.api.transaction.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class AdminCreateUserResponseDTO {
    private String userId;
    private String message;
}