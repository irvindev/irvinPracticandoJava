package com.pe.allpafood.api.transaction.order.bussiness.command;

import com.pe.allpafood.api.transaction.catalog.dto.comanda.MenuTypeDTO;
import com.pe.allpafood.api.transaction.catalog.dto.comanda.ComandaDTO;
import com.pe.allpafood.api.transaction.catalog.dto.comanda.OrderDTO;
import com.pe.allpafood.api.transaction.catalog.dto.comanda.UserRestrictionsDTO;
import com.pe.allpafood.api.transaction.order.entity.OrderSummary;
import com.pe.allpafood.api.transaction.order.repository.IOrderRepository;
import com.pe.allpafood.api.transaction.user.bussiness.impl.ProfileService;
import com.pe.allpafood.api.transaction.user.entities.ProfileEntity;
import com.pe.allpafood.api.transaction.setting.SettingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;

@Service
@RequiredArgsConstructor
public class CommandService {
    private final ProfileService profileService;
    private final IOrderRepository orderRepository;
    private final SettingRepository settingRepository;

    public ComandaDTO getCommandByDate(LocalDate ordersDate){

        List<OrderSummary> orders = orderRepository.findByDeliveryDateOrderByMenuType(ordersDate);
        List<String> complementSettings =settingRepository.findSettingValueByName("complements");

        List<OrderDTO> corporateOrders = new ArrayList<>();
        List<OrderDTO> userOrders = new ArrayList<>();
        List<OrderDTO> generalOrders = new ArrayList<>();

        Map<Integer, OrderDTO> complementsMap = new HashMap<>();
        Map<String,List<MenuTypeDTO>> uniqueUsers = new HashMap<>();

        this.generateCommand(orders, complementSettings, corporateOrders, userOrders, generalOrders, complementsMap, uniqueUsers);

        List<OrderDTO> complements = complementsMap.values().stream().toList();

        List<UserRestrictionsDTO> restrictions = uniqueUsers.isEmpty() ? null : getUserRestrictionsDTO(uniqueUsers);

        return new ComandaDTO(generalOrders, userOrders, corporateOrders, complements, restrictions);
    }

    private List<UserRestrictionsDTO> getUserRestrictionsDTO(Map<String,List<MenuTypeDTO>> usersRestrictions){
        List<String> userIds = usersRestrictions.keySet().stream().toList();
        List<ProfileEntity> userWithRestrictions = profileService.getUserAlimentsRestriction(userIds);
        List<UserRestrictionsDTO> userRestrictionsDTOs = new ArrayList<>();
        for (ProfileEntity user : userWithRestrictions) {
            UserRestrictionsDTO restrictionDTO = new UserRestrictionsDTO(
                user.getUserId(),
                user.getName().concat(" ").concat(user.getLastname()),
                usersRestrictions.get(user.getUserId()),
                user.getInformationDeserializer().getAlimentsRestrictions()
            );
            userRestrictionsDTOs.add(restrictionDTO);
        }

        return userRestrictionsDTOs;
    }

    private void generateCommand(
            List<OrderSummary> orders,
            List<String> complementSettings,
            List<OrderDTO> corporateOrders,
            List<OrderDTO> userOrders,
            List<OrderDTO> generalOrders,
            Map<Integer, OrderDTO> complementsMap,
            Map<String,List<MenuTypeDTO>> uniqueUsers
    ) {
        OrderDTO currentCorporateOrder = null;
        OrderDTO currentUserOrder = null;
        OrderDTO currentGeneralOrder = null;

        for (OrderSummary order : orders) {

            List<MenuTypeDTO> menuDTOS;
            if(order.getUserId()!=null && uniqueUsers.containsKey(order.getUserId())){
                menuDTOS = uniqueUsers.get(order.getUserId());
                menuDTOS.add(new MenuTypeDTO(
                        order.getMenuTypeId(),
                        order.getMenuName(),
                        null,
                        null
                ));
            }else{
                menuDTOS = new ArrayList<>();
                MenuTypeDTO menuDTO = new MenuTypeDTO(
                        order.getMenuTypeId(),
                        order.getMenuName(),
                        null,
                        null
                );
                menuDTOS.add(menuDTO);
                uniqueUsers.put(order.getUserId(),menuDTOS);
            }

            if (isComplement(order, complementSettings)) {
                addToComplements(order, complementsMap);
                continue;
            }

            if (order.isCorporateUser()) currentCorporateOrder = addToOrders(order, currentCorporateOrder, corporateOrders);

            if (!order.isCorporateUser()) currentUserOrder = addToOrders(order, currentUserOrder, userOrders);

            currentGeneralOrder = addToOrders(order, currentGeneralOrder, generalOrders);
        }

        addFinalOrder(currentCorporateOrder, corporateOrders);
        addFinalOrder(currentUserOrder, userOrders);
        addFinalOrder(currentGeneralOrder, generalOrders);
    }

    private boolean isComplement(OrderSummary order, List<String> complementSettings) {
        return complementSettings.contains(order.getMenuType());
    }

    private void addToComplements(OrderSummary order, Map<Integer, OrderDTO> complementsMap) {
        complementsMap.computeIfAbsent(order.getMenuTypeId(), key ->
                new OrderDTO(order.getMenuTypeId(), order.getMenuId(), order.getMenuType(),order.getMenuName(), 0)
        ).incrementCount();
    }

    private OrderDTO addToOrders(OrderSummary order, OrderDTO currentOrder, List<OrderDTO> orderList) {
        if (currentOrder == null || !currentOrder.getMenuTypeId().equals(order.getMenuTypeId())) {

            if (currentOrder != null) orderList.add(currentOrder);

            currentOrder = new OrderDTO(order.getMenuTypeId(), order.getMenuId(),order.getMenuType(), order.getMenuName(), 0);
        }
        currentOrder.incrementCount();
        return currentOrder;
    }

    private void addFinalOrder(OrderDTO currentOrder, List<OrderDTO> orderList) {
        if (currentOrder != null) orderList.add(currentOrder);
    }
}
