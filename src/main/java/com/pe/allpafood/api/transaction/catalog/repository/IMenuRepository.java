package com.pe.allpafood.api.transaction.catalog.repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.pe.allpafood.api.transaction.catalog.entity.MenuCalendar;
import com.pe.allpafood.api.transaction.catalog.entity.MenuEntity;
import com.pe.allpafood.api.transaction.catalog.entity.MenuTypeEntity;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface IMenuRepository {

    void updateMenu(MenuEntity menu);
    int insertMenu(MenuEntity menu);
    void deleteMenu(int id);
    void scheduleMenuList(List<Integer> menus, LocalDate date);
    List<MenuCalendar> findMenuBetweenCalendarDate(String userId, Integer benefitId, LocalDate startDate, LocalDate endDate);
    MenuEntity findById(Long id);
    List<Integer> findIdsByTypes(List<Integer> ids, List<String> types);
    List<MenuTypeEntity> findByDate(LocalDate date);
    List<MenuEntity> findByTypes(List<String> types);
    List<MenuEntity> findAll();
    List<MenuTypeEntity> findByTypesAndDate(LocalDate today, List<String> types);
    List<MenuEntity> findByIds(List<Integer> ids);

    List<MenuCalendar> findMenuBetweenCalendarDate(LocalDate startDate, LocalDate endDate);

    Map<String,Integer> findRandomMenusIdByDate(LocalDate date);
}
