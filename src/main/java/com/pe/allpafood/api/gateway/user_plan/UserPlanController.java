package com.pe.allpafood.api.gateway.user_plan;

import com.pe.allpafood.api.core.utils.dto.ErrorDTO;
import com.pe.allpafood.api.core.utils.dto.GenericMessage;
import com.pe.allpafood.api.transaction.plan.bussiness.impl.UserPlanService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/plan/user")
@Tag(name = "Plan del Usuario", description = "Operaciones relacionadas al plan alimenticio del usuario")
public class UserPlanController {

    private final UserPlanService userPlanService;

    public UserPlanController(UserPlanService userPlanService) {
        this.userPlanService = userPlanService;
    }

    @GetMapping("/need-day")
    @Operation(
            summary = "Obtener día necesario para el plan",
            description = "Devuelve el día que el usuario ha registrado como necesario para su plan."
    )
    public ResponseEntity<Map<String, String>> getNeedDay(
            @Parameter(description = "ID del usuario", required = true)
            @RequestAttribute String userId) {

        String needDay = userPlanService.getNeedDay(userId);
        return ResponseEntity.ok(Map.of("needDay", needDay));
    }

    @PatchMapping("/need-day")
    @Operation(
            summary = "Actualizar día necesario del plan",
            description = "Permite modificar el día que el usuario considera necesario para su plan. El campo 'needDay' debe estar presente en el cuerpo."
    )
    public ResponseEntity<Object> setNeedDay(
            @Parameter(description = "ID del usuario", required = true)
            @RequestAttribute String userId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Objeto con el campo 'needDay' que representa el nuevo valor a registrar",
                    required = true,
                    content = @io.swagger.v3.oas.annotations.media.Content(
                            mediaType = "application/json",
                            examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                                    value = "{\"needDay\": \"2025-05-01\"}"
                            )
                    )
            )
            @RequestBody Map<String, String> body) {

        String needDay = body.get("needDay");
        if (needDay == null || needDay.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(new ErrorDTO(400, "Datos son necesarios.", LocalDateTime.now()));
        }

        userPlanService.changeNeedDayInformation(userId, needDay);
        return ResponseEntity.ok(new GenericMessage("Datos actualizados correctamente."));
    }
}