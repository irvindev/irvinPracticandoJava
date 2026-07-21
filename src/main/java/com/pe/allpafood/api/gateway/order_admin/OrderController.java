package com.pe.allpafood.api.gateway.order_admin;

import com.pe.allpafood.api.core.exception.BusinessException;
import com.pe.allpafood.api.core.utils.dto.GenericMessage;
import com.pe.allpafood.api.transaction.order.dto.OrderDTO;
import com.pe.allpafood.api.transaction.order.dto.ScheduleOrderDTO;
import com.pe.allpafood.api.transaction.order.bussiness.manager.SchedulingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/order")
@RequiredArgsConstructor
public class OrderController {
    private final SchedulingService schedulingService;

    @PostMapping("/scheduled")
    public ResponseEntity<GenericMessage> postScheduled(@RequestAttribute String userId, @Valid @RequestBody List<ScheduleOrderDTO> orders) throws BusinessException {
        schedulingService.scheduledOrders(userId,orders);
        return ResponseEntity.ok(new GenericMessage("Orden creada correctamente."));
    }

    @PutMapping("/scheduled")
    public ResponseEntity<GenericMessage> updateSchedule(@RequestAttribute String userId, @RequestBody ScheduleOrderDTO request) throws BusinessException {
        if(request.orderId()==null) return ResponseEntity.badRequest().body(new GenericMessage("El id de la orden es obligatorio."));

        schedulingService.changeMenuOrder(userId,request);
        return ResponseEntity.ok(new GenericMessage("Orden actualizada correctamente."));
    }

    @GetMapping("/plan")
    public ResponseEntity<List<OrderDTO>> getOrdersFromPlan(@RequestAttribute String userId){
        return ResponseEntity.ok(schedulingService.getOrdersFromCurrentPlan(userId));
    }

    @DeleteMapping
    public ResponseEntity<?> deleteOrder(@RequestAttribute String userId, @RequestParam Long orderId){
        schedulingService.deleteOrder(userId, orderId);
        return ResponseEntity.ok(new GenericMessage("ok"));
    }
}
