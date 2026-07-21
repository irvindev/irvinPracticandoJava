package com.pe.allpafood.api.transaction.order.bussiness.validation;

import com.pe.allpafood.api.core.utils.converter.TimeUtil;
import com.pe.allpafood.api.transaction.catalog.entity.MenuTypeEntity;
import com.pe.allpafood.api.transaction.plan.entities.benefits.ConsumeBenefits;

import com.pe.allpafood.api.transaction.catalog.repository.IMenuRepository;

import com.pe.allpafood.api.transaction.order.repository.impl.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderValidationService {
    private final IMenuRepository menuRepository;
    private final OrderRepository orderRepository;

    @Value("${schedule.order.max_hour}")
    private Integer MAX_HOUR;

    public boolean isTimeExpired(LocalDate expirationDate, LocalDate scheduleDate) {
        log.info("[isTimeExpired] Starting expirationDate {} - scheduleDate {}",expirationDate,scheduleDate);
        return LocalDate.now().isAfter(expirationDate) || scheduleDate.isAfter(expirationDate);
    }

    public boolean isValidScheduleDate(LocalDate scheduleDate) {
        log.info("[isValidScheduleDate] Starting {}",scheduleDate);
        LocalDate today = TimeUtil.getPeruDate();
        LocalTime now = TimeUtil.getPeruTime();
        log.info("[isValidScheduleDate] today : {} - {}",today, now);
        if (scheduleDate.isBefore(today)) return true;
        if (scheduleDate.isEqual(today)) return true;
        return scheduleDate.isEqual(today.plusDays(1)) && now.isAfter(LocalTime.of(MAX_HOUR, 0));
    }

    public boolean hasAvailableBenefits(ConsumeBenefits consumptions, List<?> items) {
        return consumptions.getOrders().get("total") >=  (consumptions.getOrders().get("consumed") + items.size());
    }

    public boolean isMenuSelectedValid(List<Integer> menuTypeIds,List<String> principalBenefits, List<String> extras,LocalDate date) {
        log.info("[isMenuSelectedValid] Starting isMenuSelectedValid: {} - {} - {}",menuTypeIds,principalBenefits,extras);
        if (menuTypeIds.size() != principalBenefits.size())  return false;

        List<MenuTypeEntity> menusFound = menuRepository.findByDate(date);
        log.debug("[isMenuSelectedValid] Menus Found : {}",menusFound);
        List<String> menuTypeSelected = new ArrayList<>();
        List<String> menuExtras = new ArrayList<>();

        for (var menuType:menusFound){
            boolean continueProcess = false;

            for (Integer menuId : menuTypeIds){
                if (menuType.getId().equals(menuId)) {
                    menuTypeSelected.add(menuType.getType());
                    continueProcess = true;
                    break;
                }
            }

            log.debug("[isMenuSelectedValid] evaluating process: {}",continueProcess);
            if (continueProcess) continue;

            for (String extra:extras){
                if (menuType.getType().equals(extra)) menuExtras.add(menuType.getType());
            }
        }
        log.debug("[isMenuSelectedValid] menuTypeSelected: {}",menuTypeSelected);
        log.debug("[isMenuSelectedValid] menuExtras: {}",menuExtras);
        if (menuTypeSelected.size() != principalBenefits.size())  return false;
        else {
            boolean evaluatePrincipals = new HashSet<>(menuTypeSelected).equals(new HashSet<>(principalBenefits));
            if (!evaluatePrincipals) return false;
        }

        boolean result = new HashSet<>(menuExtras).equals(new HashSet<>(extras));
        log.debug("[isMenuSelectedValid] Result: {}",result);
        return result;
    }

    public boolean existByDate(String userId, LocalDate date) {
        return orderRepository.existByDeliveryDateAndUserId(date,userId);
    }
}
