package com.pe.allpafood.api.gateway.admin.users;


import com.pe.allpafood.api.gateway.admin.users.dto.DetailUserDTO;
import com.pe.allpafood.api.transaction.user.bussiness.impl.UserService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/admin/users")
@Tag(name = "Plan del Usuario", description = "Operaciones relacionadas al plan alimenticio del usuario")
@RequiredArgsConstructor
@Slf4j
public class UserAdminController {

    private final UserService userService;

    @GetMapping
    public ResponseEntity<?> getList(
            @RequestParam Integer page,
            @RequestParam Integer size,
            @RequestParam(required = false) Integer roleId,
            @RequestParam(required = false) String email
    ){
        return ResponseEntity.ok(userService.listUsers(email, roleId, page, size));
    }

    @PatchMapping("/{id}/status/{status}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateStatus(
            @PathVariable String id,
            @PathVariable Integer status
    ) {
        log.info("id {}",id);
        log.info("status {}",status);

        userService.updateUserStatus(id, status);

        return ResponseEntity.ok().body(Map.of(
                "message", "User status updated successfully"
        ));
    }


    @PostMapping()
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createUser(
            @RequestBody DetailUserDTO createUserDTO
            ) {
        userService.registerUser(createUserDTO);

        return ResponseEntity.ok().body(Map.of(
                "message", "User status updated successfully"
        ));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateUser(
            @RequestBody DetailUserDTO requestBody,
            @PathVariable String id,
            @RequestAttribute("userId") String userId
    ) {
        userService.updateInformation(requestBody, id, userId);

        return ResponseEntity.ok().body(Map.of(
                "message", "User updated successfully"
        ));
    }

}