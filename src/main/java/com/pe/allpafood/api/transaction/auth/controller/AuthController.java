package com.pe.allpafood.api.transaction.auth.controller;

import com.pe.allpafood.api.transaction.auth.application.port.AuthLoginUseCase;
import com.pe.allpafood.api.core.security.jwt.JwtUtil;
import com.pe.allpafood.api.core.exception.BusinessException;
import com.pe.allpafood.api.core.utils.dto.ErrorDTO;
import com.pe.allpafood.api.core.utils.dto.GenericMessage;
import com.pe.allpafood.api.transaction.auth.dto.PhoneVerificationDTO;
import com.pe.allpafood.api.transaction.auth.dto.SessionDTO;
import com.pe.allpafood.api.transaction.auth.dto.UserPassDTO;
import com.pe.allpafood.api.transaction.auth.dto.TokenType;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthLoginUseCase authLoginUseCase;
    private final static String ERR_MESSAGE_PHONE = "Número inválido.";
    private final static String ERR_MESSAGE_SESSION = "Sesión invalida o expirada.";
    private final static String INVALID_CREDENTIALS = "Credenciales inválidas.";
    private final static String SUCCESS_MESSAGE_LOGIN = "Inicio de sesión correctamente.";
    private final JwtUtil jwtUtil;

    @GetMapping("/validate-token")
    public ResponseEntity<?> validateToken(HttpServletRequest request){
        String authorizationHeader = request.getHeader("Authorization");
        var isValid = false;
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            var token = authorizationHeader.substring(7);
            isValid = jwtUtil.isTokenValid(token);
        }

        Map<String,Boolean> response = new HashMap<>();
        response.put("isValid",isValid);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/send-code")
    public ResponseEntity<Object> sendCode(HttpServletRequest request, @RequestBody Map<String, String> body) {
        HttpSession session = request.getSession(false);
        String phoneNumber = body.get("phoneNumber");

        if (phoneNumber == null) return ResponseEntity.status(HttpStatus.BAD_REQUEST.value()).body(new GenericMessage(ERR_MESSAGE_PHONE));

        try {
            var response = authLoginUseCase.getToken(TokenType.VERIFICATION,new PhoneVerificationDTO(phoneNumber));
            log.debug("Response Request Send Code {} ", response);
            if (session != null)  session.invalidate();
            return ResponseEntity.ok(response);
        } catch (BusinessException e) {
            log.info("BusinessException {} ", e.getMessage());
            if (session != null) session.invalidate();
            return ResponseEntity.status(HttpStatus.CONFLICT.value()).body(new GenericMessage(e.getMessage()));
        }
    }


    @GetMapping("/get-token")
    public ResponseEntity<Object> getToken(HttpServletRequest request) {
        HttpSession session = request.getSession(false);

        if (session == null || session.getAttribute("token") == null)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", ERR_MESSAGE_SESSION));

        var response = authLoginUseCase.getToken(TokenType.OAUTH2,new SessionDTO(session));
        session.invalidate();
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity<Object> login(HttpServletRequest request,@RequestBody @Valid UserPassDTO loginRequest) {
        try {
            var response = authLoginUseCase.getToken(TokenType.PASSWORD,loginRequest);
            request.getSession().invalidate();
            return ResponseEntity.ok(response);
        } catch (AuthenticationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ErrorDTO(
                    HttpStatus.CONFLICT.value(),
                    INVALID_CREDENTIALS,
                    LocalDateTime.now()
            ));
        }
    }

    @PostMapping("/logout")
        public ResponseEntity<GenericMessage> logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) session.invalidate();
        return ResponseEntity.ok(new GenericMessage(SUCCESS_MESSAGE_LOGIN));
    }
}