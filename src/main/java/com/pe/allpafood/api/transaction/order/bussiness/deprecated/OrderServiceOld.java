package com.pe.allpafood.api.transaction.order.bussiness.deprecated;

import com.pe.allpafood.api.core.enums.StatusDeliveryEnum;
import com.pe.allpafood.api.core.exception.BusinessException;
import com.pe.allpafood.api.transaction.order.dto.ScheduleOrderDTO;
import com.pe.allpafood.api.transaction.order.repository.impl.DeliveryRepository;
import com.pe.allpafood.api.transaction.order.entity.OrderUserEntity;
import com.pe.allpafood.api.transaction.plan.entities.benefits.ConsumeBenefits;
import com.pe.allpafood.api.transaction.order.entity.DeliveryPointEntity;
import com.pe.allpafood.api.transaction.order.entity.OrderEntity;
import com.pe.allpafood.api.transaction.plan.entities.UserPlanEntity;
import com.pe.allpafood.api.transaction.order.repository.IOrderRepository;
import com.pe.allpafood.api.transaction.plan.repository.IUserPlanRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderServiceOld {

    private final IOrderRepository orderRepository;
    private final DeliveryRepository deliveryRepository;
    private final IUserPlanRepository userPlanRepository;
    private final OrderUtils orderUtils;


    @Transactional
    public void completeOrder(String userId, Long orderId){
        OrderEntity order = new OrderEntity();
        order.setStatus(StatusDeliveryEnum.COMPLETED.getId());
        order.setDeliveryUserId(userId);
        order.setId(orderId);
        orderRepository.updateOrdersStatus(order);
    }

    @Transactional
    public void changeMenuOrder(String userId,ScheduleOrderDTO orderDTO) throws BusinessException {

        if (!orderRepository.existsById(orderDTO.orderId())) throw new BusinessException("No existe la orden.");

        if (orderDTO.scheduleDate()!=null) this.orderUtils.validateTimeOperation(orderDTO.scheduleDate());

        UserPlanEntity userPlan = userPlanRepository.findBenefitsConsumptionByUserId(userId);
        List<Integer> menuItems=null;

        if(orderDTO.menuTypeIds()!=null && !orderDTO.menuTypeIds().isEmpty()){
            this.orderUtils.validateMenuOperation(orderDTO.menuTypeIds(), userPlan.getConsumedBenefits().getPrincipalBenefits());
            menuItems = orderDTO.menuTypeIds().stream().toList();
        }

        this.updateOrder(orderDTO,menuItems,userId);
    }

    public void createOrder(String userId, ScheduleOrderDTO order) throws BusinessException {
        log.info("[createNewOrder] Starting... {}", userId);
        UserPlanEntity userPlan = userPlanRepository.findBenefitsConsumptionByUserId(userId);

        this.orderUtils.validateTimeExpiration(userPlan.getPlanExpirationDate(),order.scheduleDate());
        this.orderUtils.validateTimeOperation(order.scheduleDate());
        this.orderUtils.validateBenefitsConsumptionOperation(userPlan);
        this.orderUtils.validateMenuOperation(order.menuTypeIds(), userPlan.getConsumedBenefits().getPrincipalBenefits());

        this.registerOrder(userId,userPlan.getConsumedBenefits(),order);

        log.info("[createNewOrder] Finishing... {}", userId);
    }

    public void registerOrder(String userId, ConsumeBenefits consumeBenefits, ScheduleOrderDTO order){
        this.orderUtils.setExtraMenus(order.scheduleDate(),order.menuTypeIds(),consumeBenefits.getExtraBenefits());
        this.orderUtils.setComplements(order.menuTypeIds(),consumeBenefits.getComplements());

        OrderEntity orderEntity = this.mapOrderEntityFromDTO(userId,order);
        orderEntity = orderRepository.insertOrder(orderEntity);
        log.info("[register] add delivery {}",orderEntity.getDeliveryUserId());

        Integer consume = consumeBenefits.getOrders().get("consumed") + 1;
        consumeBenefits.getOrders().put("consumed",consume);
        userPlanRepository.updateBenefitsConsumption(orderEntity.getUserId(),consumeBenefits);
    }

    public void updateOrder(ScheduleOrderDTO orderDTO, List<Integer> menuItems, String userId){
        OrderEntity orderEntity = new OrderEntity();
        orderEntity.setId(orderDTO.orderId());
        orderEntity.setDeliveryDate(orderDTO.scheduleDate());
        orderEntity.setItems(menuItems);
        orderEntity.setUserId(userId);
        orderRepository.insertOrder(orderEntity);
    }

    private OrderEntity mapOrderEntityFromDTO(String userId,ScheduleOrderDTO orderDTO){
        log.info("[mapOrderEntityFromDTO] Init... {}", userId);
        OrderEntity orderEntity = new OrderEntity();
        orderEntity.setUserId(userId);
        orderEntity.setStatus(StatusDeliveryEnum.PENDING.getId());
        orderEntity.setCreateDate(LocalDateTime.now());
        orderEntity.setDeliveryDate(orderDTO.scheduleDate());
        orderEntity.setItems(orderDTO.menuTypeIds().stream().toList());

        DeliveryPointEntity deliveryPointEntity = new DeliveryPointEntity();
        orderEntity.setDeliveryPoint(deliveryPointEntity);
        log.info("[mapOrderEntityFromDTO] End... {}", userId);
        return orderEntity;
    }

}
