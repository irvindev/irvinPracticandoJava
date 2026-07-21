package com.pe.allpafood.api.transaction.order.bussiness.validation;

import com.pe.allpafood.api.core.exception.BusinessException;
import com.pe.allpafood.api.transaction.catalog.repository.IMenuRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class MenuValidationService {

    private final IMenuRepository menuRepository;

    public List<Integer> validateMenuOperation(List<Integer> menuTypeIds, List<String> principalBenefits) throws BusinessException {
        List<Integer> foundMenus = menuRepository.findIdsByTypes(menuTypeIds, principalBenefits);
        if (menuTypeIds.size() != foundMenus.size()) {
            throw new BusinessException("Los menús seleccionados no están disponibles por el momento.");
        }
        return foundMenus;
    }
}
