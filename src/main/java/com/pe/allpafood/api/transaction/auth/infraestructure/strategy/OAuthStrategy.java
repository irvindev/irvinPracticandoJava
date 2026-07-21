package com.pe.allpafood.api.transaction.auth.infraestructure.strategy;

import com.pe.allpafood.api.transaction.auth.dto.AbstractAuth;
import com.pe.allpafood.api.transaction.auth.dto.AuthResponseDTO;

import com.pe.allpafood.api.transaction.auth.dto.SessionDTO;
import com.pe.allpafood.api.transaction.plan.repository.impl.UserPlanRepository;
import com.pe.allpafood.api.transaction.auth.domain.port.AuthStrategy;
import com.pe.allpafood.api.transaction.auth.dto.TokenType;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import java.time.LocalDate;


@Service
@RequiredArgsConstructor
public class OAuthStrategy implements AuthStrategy {

    private UserPlanRepository userPlanRepository;

    @Override
    public AuthResponseDTO authenticate(AbstractAuth auth) {
        if (!(auth instanceof SessionDTO)) throw new IllegalArgumentException("Invalid request type for OAuthTokenStrategy");

        HttpSession session =((SessionDTO) auth).getSession();

        String token = (String) session.getAttribute("token");
        String provider = (String) session.getAttribute("provider");
        Boolean registrationCompleted = (Boolean) session.getAttribute("registrationCompleted");
        Boolean profileCompleted = (Boolean) session.getAttribute("profileCompleted");
        String userId = (String) session.getAttribute("userId");
        session.invalidate();

        var isPlanActive = userPlanRepository.existByUserIdAndExpDate(userId, LocalDate.now());
        return new AuthResponseDTO(token, profileCompleted, registrationCompleted, provider, isPlanActive, null, null, "USER");
    }

    @Override
    public boolean supports(TokenType type) {
        return type == TokenType.OAUTH2;
    }
}
