package com.pe.allpafood.api.transaction.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Formulario de autenticación para cambio de contraseña")
public record FormAuthentication(
        @Schema(description = "Contraseña actual", example = "password123")
        @NotNull
        String password,

        @Schema(description = "Nueva contraseña", example = "newPassword456")
        @NotNull
        String newPassword
) { }
