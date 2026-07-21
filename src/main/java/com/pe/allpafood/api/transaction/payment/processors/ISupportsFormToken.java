package com.pe.allpafood.api.transaction.payment.processors;

import com.pe.allpafood.api.transaction.payment.dto.generic.GetTokenDTO;

import java.util.Optional;

public interface ISupportsFormToken {
    Optional<String> generateFormToken(GetTokenDTO request);
}