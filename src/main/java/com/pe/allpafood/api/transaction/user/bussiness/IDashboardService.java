package com.pe.allpafood.api.transaction.user.bussiness;

import com.pe.allpafood.api.transaction.order.dto.OrderDTO;
import com.pe.allpafood.api.transaction.plan.dto.FunctionalMetricsDTO;
import com.pe.allpafood.api.transaction.plan.dto.ObjectivePoint;
import com.pe.allpafood.api.transaction.plan.dto.PlanDTO;
import com.pe.allpafood.api.transaction.catalog.entity.MenuCalendar;

import java.time.LocalDate;
import java.util.List;

public interface IDashboardService {

    FunctionalMetricsDTO getMetrics(String userId);
    List<OrderDTO> getOrdersOfWeek(String userId);
    List<MenuCalendar> getMenusOfWeek(String userId, LocalDate initialDate);
    PlanDTO getPlan(String userId);
    void registrationObjectivePoint(String userId, ObjectivePoint objectivePoint);
}
