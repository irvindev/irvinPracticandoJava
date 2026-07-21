package com.pe.allpafood.api.transaction.auth.dto;

import jakarta.servlet.http.HttpSession;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class SessionDTO extends AbstractAuth{
    private HttpSession session;
}
