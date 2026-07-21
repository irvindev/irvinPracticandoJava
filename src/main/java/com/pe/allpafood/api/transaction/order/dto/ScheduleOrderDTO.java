package com.pe.allpafood.api.transaction.order.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

public record ScheduleOrderDTO(
        @NotNull(message = "La fecha de envío es obligatoria.")
        LocalDate scheduleDate,
        @NotNull(message = "Los selección de menus es obligatoria.")
        List<Integer> menuTypeIds,
        @Null
        Long orderId,
        Long deliveryPointId
){
        public ScheduleOrderDTO {
                if (menuTypeIds != null) menuTypeIds =  new ArrayList<>(new LinkedHashSet<>(menuTypeIds));
        }
}
