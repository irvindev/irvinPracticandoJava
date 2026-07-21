package com.pe.allpafood.api.transaction.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Null;

import java.time.LocalDate;

@Schema(description = "Formulario de datos de privacidad del usuario")
public record FormPrivacyData(
        @Schema(description = "Número de documento", example = "12345678")
        String documentNumber,

        @Schema(description = "Número de teléfono", example = "+51987654321")
        String phoneNumber,

        @Schema(description = "Fecha de nacimiento", example = "1995-08-17")
        @Null
        LocalDate bornDate,

        @Schema(description = "Dirección de residencia", example = "Av. Principal 123")
        String address
) { }
