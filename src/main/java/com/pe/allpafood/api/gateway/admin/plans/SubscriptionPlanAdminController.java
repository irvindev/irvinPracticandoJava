package com.pe.allpafood.api.gateway.admin.plans;

import com.pe.allpafood.api.core.utils.dto.GenericMessage;
import com.pe.allpafood.api.gateway.admin.plans.dto.UpdateSubscriptionPlanDTO;
import com.pe.allpafood.api.transaction.plan.bussiness.IPlanService;
import com.pe.allpafood.api.transaction.plan.dto.SubscriptionPlanDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/subscription-plans")
@Tag(name = "Planes de Suscripción", description = "Mantenimiento de planes y beneficios")
@RequiredArgsConstructor
public class SubscriptionPlanAdminController {

    private final IPlanService planService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Obtener todos los planes (incluyendo no disponibles)")
    public ResponseEntity<List<SubscriptionPlanDTO>> getAll(){
        return ResponseEntity.ok(planService.getAllForAdmin());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Actualizar plan y sus beneficios")
    public ResponseEntity<GenericMessage> update(
            @PathVariable Integer id,
            @Valid @RequestBody UpdateSubscriptionPlanDTO dto){
        planService.updatePlan(id, dto);
        return ResponseEntity.status(HttpStatus.OK)
                .body(new GenericMessage("Plan actualizado correctamente."));
    }
}