package com.pe.allpafood.api.transaction.order.bussiness.export;

import com.fasterxml.jackson.core.type.TypeReference;
import com.pe.allpafood.api.core.enums.StatusDeliveryEnum;
import com.pe.allpafood.api.core.utils.converter.JsonUtil;
import com.pe.allpafood.api.transaction.order.dto.export.OrderExportDTO;
import com.pe.allpafood.api.transaction.order.entity.export.OrderExportRawRow;
import com.pe.allpafood.api.transaction.order.repository.impl.OrderExportRepository;
import com.pe.allpafood.api.transaction.user.entities.InformationEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderExportService {

    private final OrderExportRepository orderExportRepository;

    private static final String TYPE_DOUBLE_PROTEIN = "doubleprotein";
    private static final String TYPE_SNACK = "snacks";

    public List<OrderExportDTO> getOrdersForExport(LocalDate date) {
        log.info("[getOrdersForExport] Starting export for date {}", date);

        List<OrderExportRawRow> rawRows = orderExportRepository.findOrdersByDate(date);
        Map<Integer, String> menuTypesById = orderExportRepository.findMenuTypesById();

        return rawRows.stream().map(row -> mapToDTO(row, menuTypesById)).toList();
    }

    private OrderExportDTO mapToDTO(OrderExportRawRow row, Map<Integer, String> menuTypesById) {
        OrderExportDTO dto = new OrderExportDTO();
        dto.setOrderId(row.getOrderId());
        dto.setClientName(row.getClientName());
        dto.setClientLastname(row.getClientLastname());
        dto.setMotorizedName(row.getMotorizedName());
        dto.setMotorizedLastname(row.getMotorizedLastname());
        dto.setDistrict(row.getDistrict());
        dto.setStatus(StatusDeliveryEnum.fromId(row.getStatus()));

        InformationEntity information = row.getClientInformationJson() != null
                ? JsonUtil.convertToObject(row.getClientInformationJson(), InformationEntity.class)
                : null;

        dto.setSugar(information != null && Boolean.TRUE.equals(information.getSugar()) ? "Sí" : "No");
        dto.setAlimentsRestrictions(information != null && information.getAlimentsRestrictions() != null
                ? information.getAlimentsRestrictions() : "-");

        List<Integer> menuTypeIds = JsonUtil.convertToObjectList(
                row.getMenuTypeItemsJson(), new TypeReference<List<Integer>>() {});

        boolean hasDoubleProtein = false;
        boolean hasSnack = false;

        if (menuTypeIds != null) {
            for (Integer id : menuTypeIds) {
                String type = menuTypesById.get(id);
                if (type == null) continue;
                if (TYPE_DOUBLE_PROTEIN.equals(type)) hasDoubleProtein = true;
                if (TYPE_SNACK.equals(type)) hasSnack = true;
            }
        }

        dto.setDoubleProtein(hasDoubleProtein ? "Sí" : "No");
        dto.setSnack(hasSnack ? "Sí" : "No");

        return dto;
    }
}