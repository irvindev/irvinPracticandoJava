package com.pe.allpafood.api.transaction.auth.infraestructure.strategy;

import com.pe.allpafood.api.core.security.jwt.JwtUtil;
import com.pe.allpafood.api.transaction.auth.dto.AbstractAuth;
import com.pe.allpafood.api.transaction.auth.dto.AuthResponseDTO;
import com.pe.allpafood.api.transaction.auth.dto.PhoneVerificationDTO;
import com.pe.allpafood.api.transaction.auth.domain.port.AuthStrategy;
import com.pe.allpafood.api.transaction.auth.dto.TokenType;
import com.pe.allpafood.api.transaction.user.bussiness.impl.UserRegisterService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class VerifyPhone implements AuthStrategy {

    private final static String SUCCESS_MESSAGE_PHONE = "Se envío correctamente el codigo de vierificación.";
    private final UserRegisterService userRegisterService;
    private final JwtUtil jwtUtil;

    @Override
    public AuthResponseDTO authenticate(AbstractAuth auth) {
        if (!(auth instanceof PhoneVerificationDTO)) {
            throw new IllegalArgumentException("Invalid request type for VerifyPhoneTokenStrategy");
        }

        var phone = ((PhoneVerificationDTO) auth).getPhoneNumber();
        String userId = userRegisterService.sendVerificationCode(phone);
        String jwt = jwtUtil.generateToken(phone, userId, List.of("USER"));

        return new AuthResponseDTO(
                jwt,
                false,
                false,
                null,
                false,
                SUCCESS_MESSAGE_PHONE,
                null,
                "USER");
    }

    @Override
    public boolean supports(TokenType type) {
        return type == TokenType.VERIFICATION;
    }
}
