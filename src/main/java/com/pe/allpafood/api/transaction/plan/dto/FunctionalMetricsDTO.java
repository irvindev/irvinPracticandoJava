package com.pe.allpafood.api.transaction.plan.dto;


import com.pe.allpafood.api.transaction.plan.entities.ObjectiveMetric;
import com.pe.allpafood.api.transaction.plan.entities.SummaryWeek;

import java.util.List;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Métricas funcionales del usuario para mostrar en el dashboard")
public record FunctionalMetricsDTO(

    @Schema(description = "Lista de métricas registradas como objetivos semanales")
    List<ObjectiveMetric> objectivesRegistration,

    @Schema(description = "Resumen semanal de actividad y desempeño")
    List<SummaryWeek> summaryWeek,

    @Schema(description = "Edad del usuario", example = "29")
    int age,

    @Schema(description = "Altura del usuario en centímetros", example = "175")
    int height
) {}