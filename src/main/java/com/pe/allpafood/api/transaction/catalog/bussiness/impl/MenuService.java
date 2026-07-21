package com.pe.allpafood.api.transaction.catalog.bussiness.impl;

import com.pe.allpafood.api.core.exception.BusinessException;
import com.pe.allpafood.api.core.utils.converter.JsonUtil;
import com.pe.allpafood.api.core.utils.file.FileUtil;
import com.pe.allpafood.api.core.utils.generator.CodesUtil;
import com.pe.allpafood.api.transaction.catalog.bussiness.IMenusService;
import com.pe.allpafood.api.transaction.catalog.dto.MenuFormDTO;
import com.pe.allpafood.api.transaction.catalog.entity.MenuEntity;
import com.pe.allpafood.api.transaction.catalog.entity.MenuTypeEntity;
import com.pe.allpafood.api.transaction.catalog.repository.IMenuRepository;
import com.pe.allpafood.api.transaction.catalog.repository.impl.MenuTypeRepository;
import com.pe.allpafood.api.transaction.plan.entities.benefits.DetailEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@Service
@Slf4j
public class MenuService implements IMenusService {

    @Value("${application.images.path.menus}")
    private String pathMenuImages;
    @Value("${application.images.endpoint.menus}")
    private String urlImage;

    private final IMenuRepository menuRepository;
    private final MenuTypeRepository menuTypeRepository;

    public List<MenuEntity> getAllDishes() {
        return menuRepository.findAll();
    }

    public List<MenuEntity> getByIds(List<Integer> ids) {
        return menuRepository.findByIds(ids);
    }

    @Transactional
    public MenuEntity save(MenuFormDTO dto) throws IOException {
        MenuEntity menuEntity = mapToEntity(dto);

        if (dto.getImage() != null && !dto.getImage().isEmpty()){
            String originalFilename = dto.getImage().getOriginalFilename();
            String extension = "";

            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf(".") + 1);
            }
            String fileName = CodesUtil.randomId()+"."+extension;

            FileUtil.saveFile(dto.getImage(),fileName, pathMenuImages);
            menuEntity.setImageUrl(urlImage+"/"+fileName);
        }
        var hasTypes = dto.getTypes() != null && !dto.getTypes().isEmpty();

        if(dto.getId()!=null ){
            menuRepository.updateMenu(menuEntity);
            if (hasTypes) menuTypeRepository.updateStatusByMenuId(dto.getId(),0);
        }else menuRepository.insertMenu(menuEntity);


        if (hasTypes){
            List<MenuTypeEntity> menuTypes = dto.getTypes().stream()
                .map(type -> {
                    MenuTypeEntity entity = new MenuTypeEntity();
                    entity.setMenuId(dto.getId());
                    entity.setType(type);
                    entity.setStatus(1);
                    return entity;
                })
                .toList();
            menuTypeRepository.saveAll(menuTypes);
        }

        log.info("Correct save menu : {}", menuEntity);
        return menuEntity;
    }

    @Override
    public Map<String, Integer> random(LocalDate localDate) {
        return menuRepository.findRandomMenusIdByDate(localDate);
    }

    @Override
    public FileSystemResource getMenuImage(String filename) {
        File file = FileUtil.getFileFromDirectory(pathMenuImages, filename);

        if (!file.exists() || !file.isFile()) throw new BusinessException("File not found.");
        return new FileSystemResource(file);
    }

    private MenuEntity mapToEntity(MenuFormDTO dto) {
        MenuEntity menuEntity = new MenuEntity();
        menuEntity.setId(dto.getId());
        menuEntity.setDescription(dto.getDescription());
        menuEntity.setName(dto.getName());
        menuEntity.setPreviousPrice(dto.getPreviousPrice());
        menuEntity.setPrice(dto.getPrice());

        // Convertir propiedades a Float
        List<DetailEntity<Float>> parsedProps = dto.getProperties().stream().map(p -> {
            Float floatValue = Float.valueOf(p.getValue());
            return new DetailEntity<>(p.getName(), floatValue);
        }).toList();

        menuEntity.setProperties(parsedProps);

        // Muy importante: SERIALIZAR DESPUÉS de convertir
        menuEntity.setPropertiesJson(JsonUtil.convertToJsonString(parsedProps));

        return menuEntity;
    }

}
