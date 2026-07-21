package com.pe.allpafood.api.gateway.admin.plans;

import com.pe.allpafood.api.core.utils.dto.GenericMessage;
import com.pe.allpafood.api.gateway.admin.plans.dto.UpdatePlanUserDTO;
import com.pe.allpafood.api.transaction.plan.bussiness.impl.UserPlanService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/user-plan")
@Tag(name = "Plan del Usuario", description = "Operaciones relacionadas al plan alimenticio del usuario")
@RequiredArgsConstructor
@Slf4j
public class UserPlanAdminController {

    private final UserPlanService userPlanService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getList(
            @RequestParam(defaultValue = "") String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ){
        return ResponseEntity.ok(userPlanService.getUserPlansPaginated(search, page, size));
    }

    @PutMapping("/{planUserId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> update(
            @RequestBody UpdatePlanUserDTO updatePlanUserDTO,
            @PathVariable String planUserId,
            @RequestAttribute("userId") String userId
    ){
        userPlanService.updateUserPlan(planUserId, updatePlanUserDTO, userId);
        return ResponseEntity.ok(new GenericMessage("Ok"));
    }

}
