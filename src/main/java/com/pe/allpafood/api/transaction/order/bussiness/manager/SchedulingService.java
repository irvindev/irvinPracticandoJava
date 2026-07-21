package com.pe.allpafood.api.transaction.order.bussiness.manager;

import com.pe.allpafood.api.core.enums.StatusDeliveryEnum;
import com.pe.allpafood.api.core.exception.BusinessException;
import com.pe.allpafood.api.transaction.notification.bussiness.impl.NotificationOrderService;
import com.pe.allpafood.api.transaction.order.dto.OrderDTO;
import com.pe.allpafood.api.transaction.order.dto.ScheduleOrderDTO;
import com.pe.allpafood.api.transaction.order.bussiness.validation.OrderValidationService;
import com.pe.allpafood.api.transaction.order.repository.impl.DeliveryRepository;
import com.pe.allpafood.api.transaction.plan.entities.benefits.ConsumeBenefits;
import com.pe.allpafood.api.transaction.order.entity.OrderEntity;
import com.pe.allpafood.api.transaction.plan.entities.UserPlanEntity;
import com.pe.allpafood.api.transaction.plan.repository.IUserPlanRepository;
import com.pe.allpafood.api.transaction.order.repository.impl.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;


@Service
@RequiredArgsConstructor
@Slf4j
public class SchedulingService {
    private final OrderRepository orderRepository;
    private final OrderService orderService;
    private final DeliveryRepository deliveryRepository;
    private final IUserPlanRepository userPlanRepository;
    private final OrderValidationService orderValidationService;
    private final NotificationOrderService notificationOrderService;

    @Transactional
    public void scheduledOrders(String userId, List<ScheduleOrderDTO> orders) throws BusinessException {
        log.info("[create] Starting... {} - {}", userId, orders);
        UserPlanEntity userPlan = userPlanRepository.findBenefitsConsumptionByUserId(userId);
        List<Long> ids = orders.stream()
                .map(ScheduleOrderDTO::deliveryPointId)          // extrae el id
                .filter(Objects::nonNull)   // evita nulls (opcional)
                .toList();

        if (!deliveryRepository.existsIdsAndUserId(ids,userId))
            throw new BusinessException(OrdersError.DELIVERY_POINT_NOT_EXIST.getMessage());

        Long deliveryPointDefault = null;
        if (ids.size() != orders.size()){
            deliveryPointDefault = deliveryRepository.findIdAByUserIdAndAssigned(userId);
            log.debug("[create] Delivery point for user: {} {}", userId, deliveryPointDefault);
            if (deliveryPointDefault == null)
                throw new BusinessException(OrdersError.DELIVERY_POINT_NOT_SELECTED.getMessage());
        }

        if (!orderValidationService.hasAvailableBenefits(userPlan.getConsumedBenefits(), orders)){
            if (userPlan.getCredits() == null)
                throw new BusinessException(OrdersError.BENEFITS_LIMIT_EXCEEDED.getMessage());


            log.debug("[scheduledOrders] Credits for user: {} {}", userId, userPlan.getCredits());
            userPlan.setUserId(userId);
            userPlan.setConsumedBenefits(userPlan.getCredits());
            userPlan.setCredits(null);
            userPlanRepository.updateBenefitsAndCredits(userPlan);
        }

        ConsumeBenefits consumedBenefits = userPlan.getConsumedBenefits();
        log.debug("[create] Consumed benefits for user: {} {}", userId, consumedBenefits);


        for (ScheduleOrderDTO order : orders){
            log.debug("[create] Init process for : {}", order);
            if (orderValidationService.isTimeExpired(userPlan.getPlanExpirationDate(), order.scheduleDate()))
                throw new BusinessException(OrdersError.PLAN_EXPIRATION_DATE_EXCEEDED.getMessage());

            if (orderValidationService.isValidScheduleDate(order.scheduleDate()))
                throw new BusinessException(OrdersError.INVALID_SCHEDULE_DATE.getMessage());

            if (orderValidationService.existByDate(userId,order.scheduleDate()))
                throw new BusinessException(OrdersError.ALREADY_EXISTS.getMessage());

            if (!orderValidationService.isMenuSelectedValid(order.menuTypeIds(), consumedBenefits.getPrincipalBenefits(),consumedBenefits.getExtraBenefits(),order.scheduleDate()))
                throw new BusinessException(OrdersError.MENUS_NOT_AVAILABLE.getMessage());

            try {
                this.orderService.createOrder(
                        userId, 
                        order.deliveryPointId() != null 
                                ? order.deliveryPointId() 
                                :  deliveryPointDefault,
                        order, 
                        consumedBenefits);
            }catch (Exception e){
                log.error("error to create order {}", e.getMessage());
                throw new RuntimeException("error to create order");
            }

            notificationOrderService.notifyFrontend(order.scheduleDate());
        }
    }

    @Transactional
    public void completeOrder(String userId, Long orderId) {
        OrderEntity order = new OrderEntity();
        order.setStatus(StatusDeliveryEnum.COMPLETED.getId());
        order.setDeliveryUserId(userId);
        order.setId(orderId);
        orderRepository.updateOrdersStatus(order);
    }

    @Transactional
    public void changeMenuOrder(String userId,ScheduleOrderDTO orderDTO) throws BusinessException {
        log.info("[changeMenuOrder] Starting... {} - {}", userId, orderDTO);

        OrderEntity order = orderRepository.findByIdAndUserId(userId,orderDTO.orderId());
        if (order == null) throw new BusinessException("No existe la orden.");

        if (
            orderDTO.deliveryPointId() != null &&
            !order.getDeliveryPoint().getId().equals(orderDTO.deliveryPointId())
            //!order.getDeliveryPointId().equals(orderDTO.deliveryPointId())
            
        ){
            if (!deliveryRepository.existsByIdAndUserId(orderDTO.deliveryPointId().intValue(),userId))
                throw new BusinessException(OrdersError.DELIVERY_POINT_NOT_EXIST.getMessage());
        }

        log.debug("[changeMenuOrder] order {} ", order);
        if (orderDTO.scheduleDate()!=null && orderValidationService.isValidScheduleDate(orderDTO.scheduleDate()))
            throw new BusinessException("Fecha de solicitud no permitida.");

        UserPlanEntity userPlan = userPlanRepository.findBenefitsConsumptionByUserId(userId);

        if (!orderValidationService.isMenuSelectedValid(orderDTO.menuTypeIds(), userPlan.getConsumedBenefits().getPrincipalBenefits(), userPlan.getConsumedBenefits().getExtraBenefits(),order.getDeliveryDate()))
            throw new BusinessException(OrdersError.MENUS_NOT_AVAILABLE.getMessage());

        orderService.updateOrder(userId,orderDTO,order,userPlan.getConsumedBenefits());
    }

    @Transactional
    public void deleteOrder(String userId, Long orderId) {
        OrderEntity order = orderRepository.findByIdAndUserId(userId,orderId);
        if (order == null) throw new BusinessException("No existe la orden.");

        if (orderValidationService.isValidScheduleDate(order.getDeliveryDate()))
            throw new BusinessException("Fecha de solicitud no permitida.");

        LocalDate today = LocalDate.now();
        UserPlanEntity userPlan = userPlanRepository.findBenefitsConsumptionByUserId(userId);

        if (userPlan == null ||
            userPlan.getPlanExpirationDate()== null ||
            today.isAfter(userPlan.getPlanExpirationDate()))
            throw new BusinessException(OrdersError.PLAN_EXPIRATION_DATE_EXCEEDED.getMessage());

        orderRepository.deleteByOrderId(orderId);

        Integer consume = userPlan.getConsumedBenefits().getOrders().get("consumed") - 1;
        userPlan.getConsumedBenefits().getOrders().put("consumed",consume);
        userPlanRepository.updateBenefitsConsumption(userId,userPlan.getConsumedBenefits());
        log.info("[deleteOrder] Finishing new order for user {}", userId);
    }

    public List<OrderDTO> getOrdersFromCurrentPlan(String userId){
        return orderService.getCurrentOrders(userId);
    }
}
