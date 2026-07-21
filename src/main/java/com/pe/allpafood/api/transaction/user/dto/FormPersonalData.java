package com.pe.allpafood.api.transaction.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

@Schema(description = "Formulario de datos personales del usuario")
public record FormPersonalData(
        @Schema(description = "Nombre del usuario", example = "Juan")
        String name,

        @Schema(description = "Apellido del usuario", example = "Pérez")
        String lastname,

        @Schema(description = "Género del usuario", example = "Masculino")
        String gender,

        @Schema(description = "Fecha de inscripción", example = "2025-04-04")
        LocalDate registerDate,

        @Schema(description = "Id del avatar del usuario", example = "2025-04-04")
        String image

) { }
