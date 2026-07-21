package com.pe.allpafood.api.transaction.auth.application;

import com.pe.allpafood.api.transaction.auth.application.port.AuthLoginUseCase;
import com.pe.allpafood.api.transaction.auth.domain.port.AuthStrategy;
import com.pe.allpafood.api.transaction.auth.dto.AbstractAuth;
import com.pe.allpafood.api.transaction.auth.dto.AuthResponseDTO;
import com.pe.allpafood.api.transaction.auth.dto.TokenType;
import com.pe.allpafood.api.transaction.auth.infraestructure.strategy.AuthStrategyFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthLoginUseCaseImpl implements AuthLoginUseCase {

    private final AuthStrategyFactory strategyFactory;

    @Override
    public AuthResponseDTO getToken(TokenType type, AbstractAuth request) {
        AuthStrategy strategy = strategyFactory.getStrategy(type);
        return strategy.authenticate(request);
    }
}
