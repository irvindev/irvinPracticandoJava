package com.pe.allpafood.api.transaction.catalog.bussiness.impl;

import com.pe.allpafood.api.core.exception.BusinessException;
import com.pe.allpafood.api.transaction.catalog.bussiness.IExtraMenus;
import com.pe.allpafood.api.transaction.catalog.entity.MenuEntity;
import com.pe.allpafood.api.transaction.catalog.entity.MenuTypeEntity;
import com.pe.allpafood.api.transaction.catalog.repository.IMenuRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Component
public class ExtraMenus implements IExtraMenus {

    private final IMenuRepository menuRepository;

    public List<Integer> getMenuTypeIds(LocalDate scheduledDate, List<String> extraBenefits) throws BusinessException {
        log.info("[getExtraMenuIds] Starting {} - {}", extraBenefits,scheduledDate);
        if (extraBenefits == null  || extraBenefits.isEmpty()) return Collections.emptyList();

        List<MenuTypeEntity> extraMenus = menuRepository.findByTypesAndDate(scheduledDate, extraBenefits);
        log.info("[getExtraMenuIds] extraMenus found {}", extraMenus);

        List<Integer> response = extraMenus.stream()
                .collect(Collectors.toMap(MenuTypeEntity::getType, MenuTypeEntity::getId, (existing, replacement) -> existing))
                .values()
                .stream()
                .toList();

        log.info("[getExtraMenuIds] Finish {}", response);
        return response;
    }
}
