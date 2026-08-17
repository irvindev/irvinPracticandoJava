package com.pe.allpafood.api.gateway.admin.orders;

import com.pe.allpafood.api.transaction.order.bussiness.export.OrderExportService;
import com.pe.allpafood.api.transaction.order.dto.export.OrderExportDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/admin/orders")
@RequiredArgsConstructor
@Slf4j
public class OrderExportController {

    private final OrderExportService orderExportService;

    @GetMapping("/export")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<OrderExportDTO>> exportOrders(@RequestParam LocalDate date) {
        log.info("[exportOrders] Starting export for date {}", date);
        return ResponseEntity.ok(orderExportService.getOrdersForExport(date));
    }
}