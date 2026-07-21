package com.pe.allpafood.api.transaction.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.List;

public record FormUserDTO(

        @NotNull(message = "{error.name.notnull}")
        String name,

        @NotNull(message = "{error.lastname.notnull}")
        String lastname,

        @Email(message = "{error.email.invalid}")
        @NotNull(message = "{error.email.notnull}")
        String email,

        @Size(min = 8, message = "{error.password.size}")
        @NotNull(message = "{error.password.notnull}")
        //@Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$", message = "{error.password.pattern}")
        String password,

        @NotNull(message = "{error.document.size}")
        @Size(min = 8, max = 8, message = "{error.document.size}")
        @Pattern(regexp = "^[0-9]{8}$", message = "{error.document.pattern}")
        String documentNumber,

        @NotNull(message = "{error.phoneNumber.size}")
        String phoneNumber,

        List<String> districts
) {
}
