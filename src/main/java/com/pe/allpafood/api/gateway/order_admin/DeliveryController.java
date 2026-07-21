package com.pe.allpafood.api.gateway.order_admin;

import com.pe.allpafood.api.transaction.auth.dto.FormUserDTO;
import com.pe.allpafood.api.transaction.auth.dto.UpdateMotorizedDTO;
import com.pe.allpafood.api.core.utils.dto.GenericMessage;
import com.pe.allpafood.api.gateway.order_admin.dto.AssignDeliveryPoint;
import com.pe.allpafood.api.transaction.order.bussiness.DeliveryService;
import com.pe.allpafood.api.transaction.order.entity.OrderUserEntity;
import com.pe.allpafood.api.transaction.order.scheduler.AssignMotorizedProcessor;
import com.pe.allpafood.api.transaction.user.dto.DeliveryDTO;
import com.pe.allpafood.api.transaction.user.entities.UserEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/delivery")
@RequiredArgsConstructor
@Slf4j
public class DeliveryController {

    private final DeliveryService deliveryService;
    private final AssignMotorizedProcessor assignMotorizedProcessor;

    @GetMapping("motorized/find-my-orders")
    @PreAuthorize("hasRole('DELIVERY')")
    public ResponseEntity<List<OrderUserEntity>> getMotorizedOrders(
            @RequestAttribute String userId,
            @RequestParam(required = false) LocalDate date                                                        ){
        return ResponseEntity.ok(deliveryService.getAllMyOrdersToday(userId, date));
    }


    @PostMapping("motorized/complete-order")
    @PreAuthorize("hasRole('DELIVERY')")
    public ResponseEntity<Void> completeOrder(@RequestAttribute String userId,@RequestBody List<Long> orderId){
        deliveryService.completeOrders(userId,orderId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("motorized/find-users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserEntity>> getUsers(){
        return ResponseEntity.ok(deliveryService.getDeliveredUsers());
    }

    @PostMapping("motorized/create-user")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createUser(@RequestBody FormUserDTO formUserDTO){
        if (formUserDTO.districts()==null || formUserDTO.districts().isEmpty()){
            return ResponseEntity.badRequest().body(formUserDTO);
        }
        deliveryService.createNewUserDelivery(formUserDTO);
        return ResponseEntity.ok(new GenericMessage("ok"));
    }

    @PutMapping("motorized/{motorizado}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateUser(@PathVariable String motorizado, @RequestBody UpdateMotorizedDTO formUserDTO){
        deliveryService.updateUserDelivery(motorizado, formUserDTO);
        return ResponseEntity.ok(new GenericMessage("ok"));
    }

    @DeleteMapping("motorized/delete-user")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteUser(@RequestBody Map<String,String> map){

        if (!map.containsKey("userId") || map.get("userId") == null) return ResponseEntity.badRequest().body(new GenericMessage("User id es obligatorio"));
        deliveryService.deleteMotorized(map.get("userId"));
        return ResponseEntity.ok(new GenericMessage("ok"));
    }

    @GetMapping("motorized/find-all-orders")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<OrderUserEntity>> getAllOrders(
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate
    ){
        return ResponseEntity.ok(deliveryService.getAllOrders(startDate,endDate));
    }

    @PostMapping("motorized/assign-route")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> postInitRoute(@RequestBody AssignDeliveryPoint request){
        deliveryService.initDelivery(request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("motorized/unassign-route")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> postUnassignRoute(@RequestBody AssignDeliveryPoint request){
        deliveryService.deassignDelivery(request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("motorized/assign-default")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<GenericMessage> assignDefaultMotorized() {
        assignMotorizedProcessor.runAssignDefaultMotorized();
        return ResponseEntity.ok(new GenericMessage("Asignacion de motorizados ejecutada correctamente."));
    }

    @PatchMapping("/update/point")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Void> putFormDeliveryPoint(@RequestParam Long deliveryPointId, @RequestAttribute String userId){
        deliveryService.changeDeliveryPoint(userId,deliveryPointId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/create/point")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<DeliveryDTO> postNewDeliveryPoint(@RequestBody DeliveryDTO form, @RequestAttribute String userId){
        return ResponseEntity.ok(deliveryService.addDeliveryPoint(userId,form));
    }

    @DeleteMapping("/delete/point")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Void> deleteDeliveryPoint(@RequestParam Long deliveryPointId, @RequestAttribute String userId){
        deliveryService.removeDeliveryPoint(userId,deliveryPointId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/find/points")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<DeliveryDTO>> getDeliveryPoints(@RequestAttribute String userId){
        return ResponseEntity.ok(deliveryService.getMyDeliveryPoints(userId));
    }
}