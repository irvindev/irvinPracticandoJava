package com.pe.allpafood.api.transaction.order.scheduler;

import com.pe.allpafood.api.core.utils.converter.TimeUtil;
import com.pe.allpafood.api.gateway.order_admin.dto.AssignDeliveryPoint;
import com.pe.allpafood.api.transaction.order.bussiness.DeliveryService;
import com.pe.allpafood.api.transaction.order.repository.IOrderRepository;
import com.pe.allpafood.api.transaction.user.entities.UserEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.util.*;
import java.util.stream.Collectors;

/**
 *
 * @author rosalespiero1z3@gmail.com
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AssignMotorizedProcessor {

    private final IOrderRepository orderRepository;
    private final DeliveryService deliveryService;

    /**
     * Tarea programada para asignar ordenes sin motorizados
     */
    @Scheduled(cron = "${schedule.assign_motorized.cron}", zone = "America/Lima")
    public void scheduleTaskAssignDefaultMotorized() {
        runAssignDefaultMotorized();
    }

    public void runAssignDefaultMotorized() {
        log.info("Starting scheduleTaskAssignDefaultMotorized");
        var today = TimeUtil.getPeruDate().plusDays(1);

        var orders = orderRepository.findByDeliveryDateAndDeliveryUserId(today, null);
        if (orders.isEmpty()) {
            log.info("Orders empty for this date {}", today);
            return;
        }

        var users = deliveryService.getDeliveredUsers();
        if (users == null || users.isEmpty()) {
            log.error("Users (motorized) empty for this date {}", today);
            return;
        }

        Map<String, List<UserEntity>> usersByDistrict = users.stream()
                .filter(u -> u.getProfile() != null && u.getProfile().getDistrict() != null)
                .collect(Collectors.groupingBy(u -> u.getProfile().getDistrict().toLowerCase()));

        var counter = orderRepository.getTotalOrdersByDeliveryUser(today); // Map<String, Integer>

        Map<String, List<Long>> mapAssigns = new HashMap<>();

        for (var order : orders) {
            log.info("Starting scheduleTaskAssignDefaultMotorized order {}",order);
            String district = order.getOrderEntity()
                    .getDeliveryPoint()
                    .getDistrict()
                    .toLowerCase();

            var motorizeds = usersByDistrict.get(district);
            if (motorizeds == null || motorizeds.isEmpty()) {
                log.error("No hay motorizados para el distrito {}", district);
                continue;
            }

            UserEntity selected = null;
            int minOrders = Integer.MAX_VALUE;

            for (var motorized : motorizeds) {
                int motorizedCount = counter.getOrDefault(motorized.getId(), 0);
                if (motorizedCount < minOrders) {
                    minOrders = motorizedCount;
                    selected = motorized;
                }
            }

            if (selected == null) {
                log.error("Usuario no pudo ser seleccionado para distrito {}", district);
                continue;
            }

            Long orderId = order.getOrderEntity().getId();

            // ✅ agrega sin problema (lista mutable)
            mapAssigns.computeIfAbsent(selected.getId(), k -> new ArrayList<>()).add(orderId);

            // ✅ balanceo en caliente
            counter.put(selected.getId(), minOrders + 1);
        }

        for (Map.Entry<String, List<Long>> entry : mapAssigns.entrySet()) {
            var dto = new AssignDeliveryPoint(entry.getKey(), entry.getValue());
            deliveryService.initDelivery(dto);
        }
    }
}
