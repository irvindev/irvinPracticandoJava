package com.pe.allpafood.api.transaction.auth.application.port;

import com.pe.allpafood.api.transaction.auth.dto.AbstractAuth;
import com.pe.allpafood.api.transaction.auth.dto.AuthResponseDTO;
import com.pe.allpafood.api.transaction.auth.dto.TokenType;

public interface AuthLoginUseCase {
    AuthResponseDTO getToken(TokenType type, AbstractAuth request);
}
