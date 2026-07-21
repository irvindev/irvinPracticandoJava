package com.pe.allpafood.api.transaction.auth.domain.port;

import com.pe.allpafood.api.transaction.auth.dto.AbstractAuth;
import com.pe.allpafood.api.transaction.auth.dto.AuthResponseDTO;
import com.pe.allpafood.api.transaction.auth.dto.TokenType;

public interface AuthStrategy {

    AuthResponseDTO authenticate(AbstractAuth auth);

    boolean supports(TokenType type);
}
