package com.pe.allpafood.api.transaction.user.entities;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class RoleEntity {

    private Integer id;
    private String role;
}
