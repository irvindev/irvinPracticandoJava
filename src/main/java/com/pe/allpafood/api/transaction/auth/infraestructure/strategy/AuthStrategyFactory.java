package com.pe.allpafood.api.transaction.auth.infraestructure.strategy;

import com.pe.allpafood.api.transaction.auth.domain.port.AuthStrategy;
import com.pe.allpafood.api.transaction.auth.dto.TokenType;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AuthStrategyFactory {

    private final List<AuthStrategy> strategies;

    public AuthStrategyFactory(List<AuthStrategy> strategies) {
        this.strategies = strategies;
    }

    public AuthStrategy getStrategy(TokenType type) {
        return strategies.stream()
                .filter(strategy -> strategy.supports(type))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No authentication strategy found for type: " + type));
    }
}