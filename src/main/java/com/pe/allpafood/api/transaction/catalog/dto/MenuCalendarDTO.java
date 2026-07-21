package com.pe.allpafood.api.transaction.catalog.dto;

import com.pe.allpafood.api.transaction.catalog.entity.MenuEntity;

import java.time.LocalDate;
import java.util.List;

public record MenuCalendarDTO(
        LocalDate localDate,
        List<MenuEntity> menuList
){
}
