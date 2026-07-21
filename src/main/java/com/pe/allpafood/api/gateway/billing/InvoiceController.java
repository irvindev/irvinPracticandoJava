package com.pe.allpafood.api.gateway.billing;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.pe.allpafood.api.transaction.billing.dto.InvoiceDTO;
import com.pe.allpafood.api.transaction.plan.dto.UserSubscriptionDTO;

import com.pe.allpafood.api.transaction.user.dto.DeliveryDTO;
import com.pe.allpafood.api.transaction.plan.entities.benefits.DetailEntity;
import com.pe.allpafood.api.transaction.billing.entities.InvoiceAddressEntity;
import com.pe.allpafood.api.transaction.billing.bussiness.InvoiceService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;


@RestController
@RequestMapping("/invoice")
@RequiredArgsConstructor
public class InvoiceController {

    private final InvoiceService invoiceService;

    @PostMapping("/create")
    public ResponseEntity<InvoiceDTO> postCreateInvoice(
            HttpServletRequest request,
            @RequestAttribute String userId,
            @Valid @RequestBody UserSubscriptionDTO planDTO) throws JsonProcessingException {

        return ResponseEntity.ok(invoiceService.registerInvoice(userId, getClientIp(request),planDTO));
    }

    public String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
            // Puede traer varias IPs separadas por coma
            return ip.split(",")[0].trim();
        }

        ip = request.getHeader("X-Real-IP");
        if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
            return ip;
        }

        ip = request.getHeader("CF-Connecting-IP"); // si usas Cloudflare
        if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
            return ip;
        }

        return request.getRemoteAddr(); // fallback (IP del proxy si no hay headers)
    }

    @GetMapping("/list")
    public ResponseEntity<List<InvoiceDTO>> getInvoiceList(@RequestAttribute String userId){
        return ResponseEntity.ok(invoiceService.getInvoiceList(userId));
    }

    @GetMapping
    public ResponseEntity<List<DetailEntity<Float>>> getInvoiceDetail(@RequestAttribute String userId, @RequestParam String invoiceId){
        return ResponseEntity.ok(invoiceService.getInvoiceDetails(userId,invoiceId));
    }

    @GetMapping("/address")
    public ResponseEntity<InvoiceAddressEntity> getInvoiceAddress(@RequestAttribute String userId){
        return ResponseEntity.ok(invoiceService.getInvoiceAddress(userId));
    }

    @PutMapping("/address")
    public ResponseEntity<InvoiceAddressEntity> putFormInoviceAddress(@RequestBody DeliveryDTO dto, @RequestAttribute String userId){
        return ResponseEntity.ok(invoiceService.changeInvoiceAddress(userId,dto));
    }
}
