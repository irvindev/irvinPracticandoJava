package com.pe.allpafood.api.transaction.catalog.dto.comanda;

import java.util.List;

public record UserRestrictionsDTO (
        String userId,
        String fullName,
        List<MenuTypeDTO> menus,
        String restriction
){
}
