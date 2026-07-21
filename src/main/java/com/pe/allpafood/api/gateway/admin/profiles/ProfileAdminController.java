package com.pe.allpafood.api.gateway.admin.profiles;

import com.pe.allpafood.api.transaction.user.bussiness.impl.ProfileService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/users")
@Tag(name = "Plan del Usuario", description = "Operaciones relacionadas al plan alimenticio del usuario")
@RequiredArgsConstructor
@Slf4j
public class ProfileAdminController {

    private final ProfileService profileService;
}
