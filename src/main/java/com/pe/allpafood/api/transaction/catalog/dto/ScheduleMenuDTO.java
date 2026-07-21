package com.pe.allpafood.api.transaction.catalog.dto;

import java.time.LocalDate;
import java.util.List;

public record ScheduleMenuDTO (
        List<Integer> menuTypeIds,
        LocalDate date
){
}
