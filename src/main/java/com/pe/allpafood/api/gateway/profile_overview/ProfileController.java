package com.pe.allpafood.api.gateway.profile_overview;

import com.pe.allpafood.api.transaction.user.bussiness.impl.FormProfileService;
import com.pe.allpafood.api.transaction.user.dto.DeliveryDTO;
import com.pe.allpafood.api.transaction.user.dto.FormAuthentication;
import com.pe.allpafood.api.transaction.user.dto.FormPersonalData;
import com.pe.allpafood.api.transaction.user.dto.FormPrivacyData;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/profile/data")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Perfil de Usuario", description = "Operaciones relacionadas al perfil de usuario")
@SecurityRequirement(name = "bearerAuth")
public class ProfileController {

    private final FormProfileService formProfileService;

    @GetMapping("/personal")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Obtener datos personales", description = "Obtiene los datos personales del usuario como nombre, apellido y género.")
    public ResponseEntity<FormPersonalData> getPersonalData(
            @Parameter(description = "ID del usuario", required = true)
            @RequestAttribute String userId) {
        return ResponseEntity.ok(formProfileService.getUserPersonalData(userId));
    }

    @PutMapping("/personal")
    @Operation(summary = "Actualizar información personal", description = "Actualiza la información personal del usuario como nombre, apellido y género.")
    public ResponseEntity<Void> postFormPersonalInformation(
            @Valid @RequestBody FormPersonalData form,
            @Parameter(description = "ID del usuario", required = true)
            @RequestAttribute String userId) {
        formProfileService.changePersonalData(userId, form);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/privacy")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Obtener datos privados", description = "Obtiene los datos privados del usuario como n° documento, n° telefono, fecha de nacimiento y direccion.")
    public ResponseEntity<FormPrivacyData> getPrivacyData(
            @Parameter(description = "ID del usuario", required = true)
            @RequestAttribute String userId) {
        return ResponseEntity.ok(formProfileService.getUserPrivacyData(userId));
    }

    @PutMapping("/privacy")
    @Operation(summary = "Actualizar datos de privacidad", description = "Actualiza los datos de privacidad del usuario como número de documento y teléfono.")
    public ResponseEntity<Void> postFormPrivacyData(
            @RequestBody FormPrivacyData form,
            @Parameter(description = "ID del usuario", required = true)
            @RequestAttribute String userId) {
        formProfileService.changePrivacyData(userId, form);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/delivery")
    @Operation(summary = "Obtener datos de entrega", description = "Obtiene la información de entrega configurada por el usuario.")
    public ResponseEntity<DeliveryDTO> getDeliveryData(
            @Parameter(description = "ID del usuario", required = true)
            @RequestAttribute String userId) {
        return ResponseEntity.ok(formProfileService.getFormDelivery(userId));
    }

    @PutMapping("/authentication")
    @Operation(summary = "Actualizar contraseña", description = "Permite al usuario cambiar su contraseña actual por una nueva.")
    public ResponseEntity<Void> postFormAuthenticationData(
            @RequestBody FormAuthentication form,
            @Parameter(description = "ID del usuario", required = true)
            @RequestAttribute String userId) {
        formProfileService.changePassword(userId, form.password(), form.newPassword());
        return ResponseEntity.ok().build();
    }
}
