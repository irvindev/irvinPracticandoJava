package com.pe.allpafood.api.transaction.order.bussiness.deprecated;

import com.pe.allpafood.api.core.exception.BusinessException;
import com.pe.allpafood.api.transaction.catalog.entity.MenuTypeEntity;
import com.pe.allpafood.api.transaction.order.repository.impl.DeliveryRepository;
import com.pe.allpafood.api.transaction.plan.entities.benefits.ConsumeBenefits;
import com.pe.allpafood.api.transaction.catalog.entity.MenuEntity;

import com.pe.allpafood.api.transaction.plan.entities.UserPlanEntity;
import com.pe.allpafood.api.transaction.catalog.repository.IMenuRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderUtils {
    public final IMenuRepository menuRepository;
    private final DeliveryRepository deliveryRepository;

    public void validateTimeExpiration(LocalDate expirationDate, LocalDate scheduleDate)throws BusinessException {
        if (LocalDate.now().isAfter(expirationDate) || scheduleDate.isAfter(expirationDate)) throw new BusinessException("Ha superado la fecha limite de su plan.");

        log.info("[validateTimeExpiration] validation successful");
    }

    public void validateTimeOperation(LocalDate scheduleDate) throws BusinessException {
        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();

        if (scheduleDate.isBefore(today)) throw new BusinessException("No puedes solicitar órdenes para fechas anteriores.");
        if (scheduleDate.isEqual(today)) throw new BusinessException("No puedes solicitar órdenes para el día de hoy.");

        if (scheduleDate.isEqual(today.plusDays(1))) {
            if (now.isAfter(LocalTime.of(19, 0)))  throw new BusinessException("Las órdenes para mañana deben solicitarse antes de las 7:00 PM del día.");
        }

        log.info("[validateTimeOperation] validation successful");
    }

    public void validateBenefitsConsumptionOperation(UserPlanEntity userPlan) throws BusinessException {
        ConsumeBenefits consumptions = userPlan.getConsumedBenefits();
        if (consumptions.getOrders().get("total") <= consumptions.getOrders().get("consumed"))  throw new BusinessException("Ha superado el limite de beneficios de su plan.");
        log.info("[validateBenefitsConsumptionOperation] validation successful");
    }

    public void validateMenuOperation(List<Integer> menuTypeIds, List<String> principalBenefits) throws BusinessException {
        List<Integer> foundMenus = menuRepository.findIdsByTypes(menuTypeIds,principalBenefits);

        if (menuTypeIds.size() != foundMenus.size()) throw new BusinessException("Los menus seleccionados no están disponibles por el momento.");
        log.info("[validateMenuOperation] validation successful");
    }

    public void validateAddressOperation(String userId, Integer addressId) throws BusinessException {
        if(!deliveryRepository.existsByIdAndUserId(addressId, userId)) throw new BusinessException("La dirección que selecciono no se encuetra disponible.");
        log.info("[validateAddressOperation] validation successful");
    }


    public void setExtraMenus(LocalDate scheduledDate, List<Integer> menusId, List<String> extraBenefits) throws BusinessException {

        if (!extraBenefits.isEmpty()){

            List<MenuTypeEntity> extraMenus = menuRepository.findByTypesAndDate(scheduledDate,extraBenefits);

            if(extraBenefits.size()==extraMenus.size()){
                log.info("[setExtraMenus] finded extraMenus {} ",extraMenus);

                Map<String, Long> typeCount = extraMenus.stream()
                        .collect(Collectors.groupingBy(MenuTypeEntity::getType, Collectors.counting()));

                boolean isTypeRepeated = typeCount.values().stream().anyMatch(count -> count > 1);

                if(isTypeRepeated) menusId.addAll(extraMenus.stream().map(MenuTypeEntity::getId).toList());
            }else{
                throw new BusinessException("Los menus no se encuentra programados para esta fecha.");
            }
        }
    }

    public void setComplements(List<Integer> menusId ,List<Integer> complements){
        if (!complements.isEmpty()){
            List<MenuEntity> extraMenus = menuRepository.findByIds(complements);

            if(complements.size() == extraMenus.size()){
                log.info("[setComplements] finded extraMenus {} ",extraMenus);
                menusId.addAll(extraMenus.stream().map(MenuEntity::getId).toList());
            }else{
                throw new BusinessException("Los menus no se encuentra programados para esta fecha.");
            }
        }
    }
}
