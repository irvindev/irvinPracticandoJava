package com.pe.allpafood.api.transaction.order.bussiness.manager;

import com.pe.allpafood.api.core.enums.StatusDeliveryEnum;
import com.pe.allpafood.api.core.utils.converter.TimeUtil;
import com.pe.allpafood.api.transaction.catalog.bussiness.IComplementMenus;
import com.pe.allpafood.api.transaction.catalog.bussiness.IExtraMenus;
import com.pe.allpafood.api.transaction.order.dto.OrderDTO;
import com.pe.allpafood.api.transaction.order.dto.ScheduleOrderDTO;
import com.pe.allpafood.api.transaction.plan.entities.benefits.ConsumeBenefits;
import com.pe.allpafood.api.transaction.order.entity.OrderEntity;
import com.pe.allpafood.api.transaction.plan.entities.UserPlanEntity;
import com.pe.allpafood.api.transaction.order.repository.IOrderRepository;
import com.pe.allpafood.api.transaction.plan.repository.IUserPlanRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderService {
    private final IOrderRepository orderRepository;
    private final IUserPlanRepository userPlanRepository;
    private final IComplementMenus complementMenus;
    private final IExtraMenus extraMenus;

    public void createOrder(String userId, Long deliveryPointId, ScheduleOrderDTO order, ConsumeBenefits consumeBenefits) {
        log.info("[createOrder] Registering new order for user {}", userId);

        order.menuTypeIds().addAll(extraMenus.getMenuTypeIds(order.scheduleDate(), consumeBenefits.getExtraBenefits()));
        order.menuTypeIds().addAll(extraMenus.getMenuTypeIds(order.scheduleDate(), consumeBenefits.getAdditional()));
        order.menuTypeIds().addAll(complementMenus.getMenuIds(consumeBenefits.getComplements()));

        OrderEntity orderEntity = this.mapOrderEntityFromDTO(userId,deliveryPointId,order);
        orderEntity = orderRepository.insertOrder(orderEntity);
        log.info("[register] add delivery {}",orderEntity.getDeliveryUserId());

        Integer consume = consumeBenefits.getOrders().get("consumed") + 1;
        consumeBenefits.getOrders().put("consumed",consume);
        userPlanRepository.updateBenefitsConsumption(orderEntity.getUserId(),consumeBenefits);
        log.info("[register] Finishing new order for user {}", userId);
    }

    public void updateOrder(String userId,ScheduleOrderDTO orderDTO,OrderEntity order, ConsumeBenefits consumeBenefits){
        orderDTO.menuTypeIds().addAll(extraMenus.getMenuTypeIds(order.getDeliveryDate(),consumeBenefits.getExtraBenefits()));
        orderDTO.menuTypeIds().addAll(complementMenus.getMenuIds(consumeBenefits.getComplements()));
        OrderEntity orderEntity = new OrderEntity();
        orderEntity.setId(orderDTO.orderId());
        orderEntity.setItems(orderDTO.menuTypeIds());
        orderEntity.setUserId(userId);
        orderEntity.setDeliveryPointId(orderDTO.deliveryPointId());
        orderRepository.updateOrder(orderEntity);
    }

    public List<OrderDTO> getCurrentOrders(String userId) {
        log.info("[getOrdersByCurrentPlan] Starting... UserId {}",userId);

        var today = TimeUtil.getPeruDate();
        UserPlanEntity userPlan = userPlanRepository.findBenefitsConsumptionByUserId(userId);

        if (userPlan == null || userPlan.getPlanExpirationDate() == null || today.isAfter(userPlan.getPlanExpirationDate()))
            return new ArrayList<>();

        List<OrderEntity> orderEntities = orderRepository.findMenusByDateRangesAndUserId(userId, userPlan.getPlanInitDate(), userPlan.getPlanExpirationDate());
        log.info("[getOrdersByCurrentPlan] Orders: {}",orderEntities);
        List<OrderDTO> response = new ArrayList<>();

        for (OrderEntity entity : orderEntities) {
            OrderDTO orderDTO = new OrderDTO(
                    entity.getId(),
                    entity.getDeliveryDate(),
                    entity.getMenuTypes()
            );
            response.add(orderDTO);
        }

        return response;
    }

    public OrderEntity getOrderByDate(String userId, LocalDate deliveryDate){
        return orderRepository.findByDeliveryDateAndUserId(deliveryDate,userId);
    }

    private OrderEntity mapOrderEntityFromDTO(String userId, Long deliveryPointId,ScheduleOrderDTO orderDTO) {
        OrderEntity orderEntity = new OrderEntity();
        orderEntity.setUserId(userId);
        orderEntity.setStatus(StatusDeliveryEnum.PENDING.getId());
        orderEntity.setCreateDate(LocalDateTime.now());
        orderEntity.setDeliveryDate(orderDTO.scheduleDate());
        orderEntity.setDeliveryPointId(deliveryPointId);
        orderEntity.setItems(orderDTO.menuTypeIds());
        return orderEntity;
    }

}
