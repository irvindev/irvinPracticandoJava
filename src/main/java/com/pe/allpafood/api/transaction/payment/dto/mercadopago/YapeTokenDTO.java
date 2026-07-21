package com.pe.allpafood.api.transaction.payment.dto.mercadopago;

import com.pe.allpafood.api.transaction.payment.dto.generic.GetTokenDTO;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class YapeTokenDTO extends GetTokenDTO{
        @NotNull
        private String phone;

        @NotNull
        private String otp;

        @NotNull
        private String requestId;

        public void setMethod(String method){
                super.setMethod(method);
        }
}
