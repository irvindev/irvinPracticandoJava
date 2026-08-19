package com.pe.allpafood.api.transaction.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AdminCreateUserDTO(

        @NotNull(message = "El nombre es obligatorio.")
        @NotBlank(message = "El nombre es obligatorio.")
        String name,

        @NotNull(message = "El apellido es obligatorio.")
        @NotBlank(message = "El apellido es obligatorio.")
        String lastname,

        @NotNull(message = "El email es obligatorio.")
        @Email(message = "El email no es válido.")
        @Size(max = 150, message = "El correo no puede exceder 150 caracteres.")
        String email,

        @NotNull(message = "El teléfono es obligatorio.")
        String phoneNumber,

        @NotNull(message = "El número de documento es obligatorio.")
        @Size(max = 10, message = "El número de documento no puede exceder 10 caracteres.")
        String documentNumber,

        @NotNull(message = "La contraseña es obligatoria.")
        @Size(min = 8, message = "La contraseña debe tener al menos 8 caracteres.")
        String password
) {
}