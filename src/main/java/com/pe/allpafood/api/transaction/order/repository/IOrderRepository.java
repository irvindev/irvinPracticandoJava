package com.pe.allpafood.api.transaction.order.repository;

import com.pe.allpafood.api.transaction.order.entity.OrderSummary;
import com.pe.allpafood.api.transaction.order.entity.OrderUserEntity;
import com.pe.allpafood.api.transaction.order.entity.OrderEntity;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface IOrderRepository {

    Map<String, Integer> getTotalOrdersByDeliveryUser(LocalDate deliveryDate);

    List<OrderUserEntity> findByDeliveryDateAndDeliveryUserId(LocalDate today, String deliveryUserId);
    List<OrderUserEntity> findByDeliveryDateAndStatus(LocalDate today,LocalDate endDate);
    void updateOrdersStatusAndMotorized(List<OrderEntity> orders, String statusBase, String userIdBase);
    void updateOrdersStatus(OrderEntity order);
    OrderEntity insertOrder(OrderEntity order);
    void updateOrder(OrderEntity order);
    List<OrderEntity> findMenusByDateRangesAndUserId(String userId, LocalDate init, LocalDate end);
    List<OrderSummary> findByDeliveryDateOrderByMenuType(LocalDate deliveryDate);
    boolean existsById(Long id);
    OrderEntity findByDeliveryDateAndUserId(LocalDate deliveryDate, String userId);
    boolean existByDeliveryDateAndUserId(LocalDate deliveryDate, String userId);
    List<String> findPhoneNumbersByOrderIds(List<Long> orderIds);
    void updateByUserIdAndStatusAndMinDate(String userId, LocalDate deliveryDate, String status,Long deliveryPointId);
    void deleteByOrderId(Long orderId);
    OrderEntity findByIdAndUserId(String userId, Long id);
}
