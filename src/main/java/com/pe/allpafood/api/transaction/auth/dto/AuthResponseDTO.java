package com.pe.allpafood.api.transaction.auth.dto;

import com.pe.allpafood.api.transaction.user.dto.ProfileDTO;

public record AuthResponseDTO(
        String token,
        Boolean profileCompleted,
        Boolean registrationCompleted,
        String provider,
        Boolean planActive,
        String message,
        ProfileDTO profile,
        String role
){
}
