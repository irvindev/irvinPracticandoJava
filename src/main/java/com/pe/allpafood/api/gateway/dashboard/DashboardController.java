package com.pe.allpafood.api.gateway.dashboard;

import com.pe.allpafood.api.core.utils.converter.TimeUtil;
import com.pe.allpafood.api.core.utils.dto.ErrorDTO;
import com.pe.allpafood.api.core.utils.dto.GenericMessage;
import com.pe.allpafood.api.transaction.plan.dto.FunctionalMetricsDTO;
import com.pe.allpafood.api.transaction.order.dto.OrderDTO;
import com.pe.allpafood.api.transaction.plan.dto.ObjectivePoint;
import com.pe.allpafood.api.transaction.plan.dto.PlanDTO;
import com.pe.allpafood.api.transaction.user.bussiness.IDashboardService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/dashboard")
@RequiredArgsConstructor
@Tag(name = "Dashboard", description = "Operaciones relacionadas al panel del usuario")
public class DashboardController {

    private final IDashboardService dashboardService;

    @GetMapping("/metrics")
    @Operation(
            summary = "Obtener métricas funcionales",
            description = "Devuelve las métricas relacionadas al progreso y estado general del usuario en la plataforma."
    )
    public ResponseEntity<FunctionalMetricsDTO> getDashboard(
            @Parameter(description = "ID del usuario", required = true)
            @RequestAttribute String userId) {
        return ResponseEntity.ok(dashboardService.getMetrics(userId));
    }

    @GetMapping("/plan")
    @Operation(
            summary = "Obtener información del plan del usuario",
            description = "Devuelve los datos actuales del plan de alimentación asignado al usuario."
    )
    public ResponseEntity<PlanDTO> getUserPlan(
            @Parameter(description = "ID del usuario", required = true)
            @RequestAttribute String userId) {
        return ResponseEntity.ok(dashboardService.getPlan(userId));
    }

    @GetMapping("/menus")
    @Operation(
            summary = "Obtener menú semanal",
            description = "Obtiene el menú correspondiente a la semana de la fecha inicial proporcionada. La fecha no puede ser anterior a la fecha actual."
    )
    public ResponseEntity<?> getMenuOfWeek(
            @Parameter(description = "ID del usuario", required = true)
            @RequestAttribute String userId,
            @Parameter(description = "Fecha inicial de la semana", example = "2025-05-01", required = true)
            @RequestParam LocalDate initDate) {

        if (initDate.isBefore(TimeUtil.getPeruDate())) {
            return ResponseEntity.badRequest()
                    .body(new ErrorDTO(400, "No se puede obtener los menús de fechas anteriores", LocalDateTime.now()));
        }

        return ResponseEntity.ok(dashboardService.getMenusOfWeek(userId, initDate));
    }

    @GetMapping("/orders")
    @Operation(
            summary = "Obtener órdenes semanales",
            description = "Devuelve la lista de órdenes registradas por el usuario para la semana actual."
    )
    public ResponseEntity<List<OrderDTO>> getOrderOfWeek(
            @Parameter(description = "ID del usuario", required = true)
            @RequestAttribute String userId) {
        return ResponseEntity.ok(dashboardService.getOrdersOfWeek(userId));
    }

    @PostMapping("/metrics/objective")
    @Operation(
            summary = "Registrar objetivo del usuario",
            description = "Permite registrar el objetivo semanal del usuario para el seguimiento de sus métricas personales."
    )
    public ResponseEntity<?> postObjective(
            @Parameter(description = "ID del usuario", required = true)
            @RequestAttribute String userId,
            @Parameter(description = "Datos del objetivo a registrar", required = true)
            @Valid @RequestBody ObjectivePoint request) {

        dashboardService.registrationObjectivePoint(userId, request);
        return ResponseEntity.ok(new GenericMessage("ok"));
    }
}