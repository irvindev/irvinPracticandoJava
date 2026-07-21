package com.pe.allpafood.api.gateway.payments;

import com.pe.allpafood.api.core.utils.file.FileUtil;
import com.pe.allpafood.api.core.exception.BusinessException;
import com.pe.allpafood.api.core.utils.dto.GenericMessage;
import com.pe.allpafood.api.transaction.payment.dto.TransactionDTO;
import com.pe.allpafood.api.transaction.payment.bussiness.impl.PaymentService;
import com.pe.allpafood.api.transaction.payment.dto.generic.GetTokenDTO;
import com.pe.allpafood.api.transaction.payment.dto.mercadopago.YapeTokenDTO;
import com.pe.allpafood.api.transaction.payment.processors.IPaymentProcessor;
import com.pe.allpafood.api.transaction.payment.processors.ISupportsFormToken;
import com.pe.allpafood.api.transaction.payment.processors.ProcessorFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/subscriptions/payments/secure")
@RequiredArgsConstructor
@Slf4j
public class PaymentSecureController {


    private final static List<String> ALLOW_EXTENSIONS = Arrays.asList("PNG","JPEG","JPG");
    private final static String REGEX_PHONE_NUMBER = "^9\\d{8}$";
    private final static List<String> ALLOW_METHOD_PAYMENT = Arrays.asList("yape","plin");
    private final PaymentService paymentService;
    private final ProcessorFactory processorFactory;

    @PostMapping("/send-voucher")
    public ResponseEntity<GenericMessage> send (@RequestAttribute String userId,
                                @RequestParam("voucher") MultipartFile file,
                                @RequestParam String phoneNumber,
                                @RequestParam String invoiceId,
                                @RequestParam String methodPayment){


        if (FileUtil.isValidFile(file,ALLOW_EXTENSIONS)) return  ResponseEntity.badRequest().body(new GenericMessage("Formato de imagen inválido."));

        if(phoneNumber==null || !phoneNumber.matches(REGEX_PHONE_NUMBER)) return  ResponseEntity.badRequest().body(new GenericMessage("Número inválido."));

        if (!ALLOW_METHOD_PAYMENT.contains(methodPayment)) return  ResponseEntity.badRequest().body(new GenericMessage("Metódo de pago inválido."));

        try{
            paymentService.createPaymentVocuher(file,userId,invoiceId,methodPayment,phoneNumber);
            return ResponseEntity.ok(new GenericMessage("Se envío el voucher de pago correctamente."));
        }catch (BusinessException e){
            return  ResponseEntity.status(HttpStatus.CONFLICT.value()).body(new GenericMessage(e.getMessage()));
        }
    }

    @PostMapping("/charge")
    public ResponseEntity<Object> postChargePayment(@RequestAttribute String userId, @RequestBody TransactionDTO transactionDTO) throws BusinessException {
        paymentService.createTransactionPayment(userId,transactionDTO);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/tokens/{processor}/{method}")
    public ResponseEntity<Object> generateTokens(
            @PathVariable String processor,
            @PathVariable String method,
            @RequestBody YapeTokenDTO tokenDTO) throws BusinessException {
        IPaymentProcessor paymentProcessor = processorFactory.getProcessor(processor);
        log.info("Generating tokens for payment processor {}", paymentProcessor);
        if (paymentProcessor.isAsync()) {
            log.info("Generating tokens for payment processor {}", paymentProcessor.isAsync());
            try {
                tokenDTO.setMethod(method);
                ISupportsFormToken tokenCapable = (ISupportsFormToken) paymentProcessor;
                return ResponseEntity.ok().body(Map.of("token",tokenCapable.generateFormToken(tokenDTO)));
            } catch (Exception e) {
                log.error("Error generating form token for method '{}': {}", method, e.getMessage());
                return ResponseEntity.internalServerError().body("");
            }
        } else {
            log.warn("Processor '{}' does not support async operations (form token not supported).", method);
            return ResponseEntity.internalServerError().body("");
        }
    }
}
