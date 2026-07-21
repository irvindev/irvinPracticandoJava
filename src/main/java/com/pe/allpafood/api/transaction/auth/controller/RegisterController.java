package com.pe.allpafood.api.transaction.auth.controller;

import com.pe.allpafood.api.core.exception.BusinessException;
import com.pe.allpafood.api.core.utils.dto.GenericMessage;
import com.pe.allpafood.api.transaction.auth.dto.FormUserDTO;
import com.pe.allpafood.api.transaction.user.dto.ProfileDTO;
import com.pe.allpafood.api.transaction.user.entities.UserEntity;
import com.pe.allpafood.api.transaction.user.bussiness.impl.ProfileService;
import com.pe.allpafood.api.transaction.user.bussiness.impl.UserRegisterService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/register")
@RequiredArgsConstructor
@Slf4j
public class RegisterController {
    private final UserRegisterService userRegisterService;
    private final ProfileService profileService;

    @PostMapping("/user")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<UserEntity> register(@RequestAttribute String userId,@Valid @RequestBody FormUserDTO request) {
        return ResponseEntity.ok().body(userRegisterService.registerUser(userId,request));
    }

    @PutMapping("/profile")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ProfileDTO> putProfile(@RequestAttribute String userId, @Valid @RequestBody ProfileDTO request) {
        return ResponseEntity.ok(profileService.completeProfile(userId,request));
    }

    @PostMapping("/verify-code")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Object> codeVerification(@RequestAttribute String userId, @RequestBody Map<String,String> request) {
        String code = request.get("code");

        if (code == null) return new ResponseEntity<>(new GenericMessage("Código inválido."), HttpStatus.BAD_REQUEST);

        userRegisterService.verifyCode(userId,code);

        Map<String,String> response = new HashMap<>();
        response.put("message","El código ser verifico correctamente.");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/upload-massive")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<GenericMessage> uploadUser(@RequestParam("file") MultipartFile file,@RequestParam("corporationId") Integer corporationId,@RequestParam Integer planId,@RequestParam(required = false) List<Integer> complements) throws BusinessException {

        log.info("Iniciando uploadMassiveUser");
        userRegisterService.uploadMassive(file,corporationId,planId,complements);
        return  ResponseEntity.ok(new GenericMessage("CSV cargado correctamente."));
    }
}
