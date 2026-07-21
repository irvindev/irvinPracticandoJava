package com.pe.allpafood.api.transaction.catalog.bussiness.impl;

import com.pe.allpafood.api.core.exception.BusinessException;
import com.pe.allpafood.api.transaction.setting.SettingRepository;
import com.pe.allpafood.api.transaction.catalog.bussiness.IComplementMenus;
import com.pe.allpafood.api.transaction.catalog.entity.MenuEntity;
import com.pe.allpafood.api.transaction.catalog.repository.impl.MenuRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Component
public class ComplementMenus implements IComplementMenus {
    private final SettingRepository settingRepository;
    private final MenuRepository menuRepository;

    public List<MenuEntity> getAll() {
        List<String> types = settingRepository.findSettingValueByName("complements");
        return menuRepository.findByTypes(types);
    }

    public List<Integer> getMenuIds(List<Integer> complements) throws BusinessException {
        log.info("[ComplementMenus] Starting {} ", complements);
        if (complements == null || complements.isEmpty()) return Collections.emptyList();

        List<MenuEntity> extraMenus = menuRepository.findByIds(complements);
        log.info("[ComplementMenus] Complement menus found: {}", extraMenus);
        return extraMenus.stream().map(MenuEntity::getId).toList();
    }
}
