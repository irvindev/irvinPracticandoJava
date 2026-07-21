package com.pe.allpafood.api.transaction.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class PhoneVerificationDTO extends AbstractAuth{
        private String phoneNumber;
}
