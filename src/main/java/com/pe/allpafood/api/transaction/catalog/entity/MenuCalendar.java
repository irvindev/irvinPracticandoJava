package com.pe.allpafood.api.transaction.catalog.entity;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class MenuCalendar {
    private LocalDate localDate;
    private List<MenuTypeGroup> menuTypeGroups;
}
