package com.pe.allpafood.api.transaction.catalog.bussiness;

import com.pe.allpafood.api.transaction.catalog.entity.MenuEntity;

import java.util.List;

public interface IComplementMenus {
    List<MenuEntity> getAll() ;
    List<Integer> getMenuIds(List<Integer> complements);
}
