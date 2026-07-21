package com.pe.allpafood.api.transaction.catalog.bussiness;

import com.pe.allpafood.api.transaction.catalog.dto.MenuFormDTO;
import com.pe.allpafood.api.transaction.catalog.entity.MenuEntity;
import org.springframework.core.io.FileSystemResource;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface IMenusService {
    List<MenuEntity> getAllDishes();
    MenuEntity save(MenuFormDTO dto) throws IOException;
    Map<String, Integer> random(LocalDate localDate);
    FileSystemResource getMenuImage(String filename);
}
