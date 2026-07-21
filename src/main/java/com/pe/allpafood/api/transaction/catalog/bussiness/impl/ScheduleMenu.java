package com.pe.allpafood.api.transaction.catalog.bussiness.impl;

import com.pe.allpafood.api.core.exception.BusinessException;
import com.pe.allpafood.api.transaction.catalog.dto.ScheduleMenuDTO;
import com.pe.allpafood.api.transaction.catalog.entity.MenuCalendar;
import com.pe.allpafood.api.transaction.catalog.entity.MenuEntity;
import com.pe.allpafood.api.transaction.catalog.entity.MenuTypeEntity;
import com.pe.allpafood.api.transaction.catalog.repository.IMenuRepository;
import com.pe.allpafood.api.transaction.catalog.repository.impl.MenuTypeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class ScheduleMenu {
    private final IMenuRepository menuRepository;
    private final MenuTypeRepository menuTypeRepository;

    @Transactional
    public void schedule(ScheduleMenuDTO dto){
        if (dto.menuTypeIds().size() != new HashSet<>(dto.menuTypeIds()).size())
            throw new BusinessException("No puedes registrar el mismo menú para esta fecha.");

        var menuTypeEntities = menuTypeRepository.findByIds(dto.menuTypeIds());

        if (menuTypeEntities.size() != dto.menuTypeIds().size()) throw new BusinessException("El menu seleccionado no existe.");

        int countLunch = 0;
        int countDinner = 0;
        int countBreakfast = 0;
        int countDrink = 0;
        int countSnack = 0;
        for (var menuType : menuTypeEntities) {
            int MAX_ALLOWED_LUNCH_DINNER = 3;
            switch (menuType.getType()) {
                case "lunch":
                    if ( countLunch >= MAX_ALLOWED_LUNCH_DINNER) throw new BusinessException("No puedes ingresar mas de 3 almuerzos.");
                    countLunch++;
                    break;
                case "dinner":
                    if ( countDinner >= MAX_ALLOWED_LUNCH_DINNER) throw new BusinessException("No puedes ingresar mas de 3 cenas.");
                    countDinner++;
                    break;
                case "breakfast":
                    if ( countBreakfast >= 1) throw new BusinessException("Solo puedes ingresar un desayuno.");
                    countBreakfast++;
                    break;
                case "drinks":
                    if ( countDrink >= 1) throw new BusinessException("Solo puedes ingresar una bebida.");
                    countDrink++;
                    break;
                case "snacks":
                    if ( countSnack >= 1) throw new BusinessException("Solo puedes ingresar un snack.");
                    countSnack++;
                    break;
                default:
                    throw new BusinessException("Tipo desconocido");
            }
        }

        menuRepository.scheduleMenuList(dto.menuTypeIds(),dto.date());
    }


    @Transactional
    public List<MenuCalendar> getScheduledMenu(LocalDate startDate, LocalDate endDate){
        return menuRepository.findMenuBetweenCalendarDate(startDate,endDate);
    }

}
