package com.pe.allpafood.api.transaction.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.List;

public record UpdateMotorizedDTO(

        String name,

        String lastname,

        @Email(message = "{error.email.invalid}")
        String email,

        @Size(min = 8, message = "{error.password.size}")
        String password,

        @Size(min = 8, max = 8, message = "{error.document.size}")
        @Pattern(regexp = "^[0-9]{8}$", message = "{error.document.pattern}")
        String documentNumber,

        String phoneNumber,

        List<String> districts
) {
}