package com.pe.allpafood.api.gateway.admin.users.dto;

import java.util.List;

public record DetailUserDTO (
        String name,
        String lastname,
        String email,
        String password,
        String documentNumber,
        String phoneNumber,
        String roleId,
        Integer status,
        List<String> districts
){}
