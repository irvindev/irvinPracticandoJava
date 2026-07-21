package com.pe.allpafood.api.transaction.order.scheduler;

import com.pe.allpafood.api.core.exception.BusinessException;
import com.pe.allpafood.api.core.utils.converter.TimeUtil;
import com.pe.allpafood.api.transaction.catalog.bussiness.IMenusService;
import com.pe.allpafood.api.transaction.order.dto.ScheduleOrderDTO;
import com.pe.allpafood.api.transaction.order.entity.OrderEntity;
import com.pe.allpafood.api.transaction.order.bussiness.manager.OrderService;
import com.pe.allpafood.api.transaction.order.repository.impl.DeliveryRepository;
import com.pe.allpafood.api.transaction.plan.entities.UserPlanEntity;
import com.pe.allpafood.api.transaction.plan.repository.IUserPlanRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.*;

/**
 * Componente encargado de procesar y registrar órdenes por defecto para usuarios con planes activos.
 * <p>
 * Esta clase ejecuta una tarea programada cada semana para generar órdenes para la semana siguiente,
 * respetando la fecha de expiración del plan del usuario y sus beneficios consumidos.
 * </p>
 *
 * @author rosalespiero1z3@gmail.com
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class OrderProcessor {

    private final IUserPlanRepository userPlanRepository;
    private final OrderService orderService;
    private final DeliveryRepository deliveryRepository;
    private final IMenusService menusService;

    /**
     * Tarea programada que crea órdenes por defecto para los usuarios que tienen planes activos
     * para la próxima semana (de lunes a viernes).

     * Se ejecuta según el cron definido en la propiedad {@code schedule.orders.cron}.
     */
    @Scheduled(cron = "${schedule.orders.cron}", zone = "America/Lima")
    public void scheduleTaskCreateDefaultOrders() {
        LocalDate today = LocalDate.from(TimeUtil.getPeruDateTime());
        LocalDate nextMonday = getNextDay(today);
        LocalDate nextFriday = nextMonday.plusDays(4);

        log.info("[scheduleTaskCreateDefaultOrders] Starting with params {} - {}", nextMonday, nextFriday);
        List<UserPlanEntity> users = userPlanRepository.getAllUserPlansAvailableByDate(LocalDate.now());

        for (UserPlanEntity user : users){
            this.process(user,nextMonday,nextFriday);
        }
        log.info("[scheduleTaskCreateDefaultOrders] End all process with params {} - {}", nextMonday, nextFriday);
    }

    /**
     * Procesa el plan de un usuario entre un rango de fechas. Si no hay orden registrada
     * y el usuario aún tiene beneficios, se genera una orden con menús predeterminados.
     *
     * @param user     Entidad que representa el plan del usuario.
     * @param initDay  Día de inicio del rango (normalmente lunes).
     * @param endDay   Día de fin del rango (normalmente viernes).
     */
    public void process(UserPlanEntity user, LocalDate initDay, LocalDate endDay){
        log.info("[process] Starting process : {} {} {}", initDay, endDay, user);

        Long deliveryPoint = deliveryRepository.findIdAByUserIdAndAssigned(user.getUserId());

        if (deliveryPoint == null || user.getPlanExpirationDate() == null) {
            log.error("[process] Terminate process, Delivery point or planExpirationDate not found for : {}", user);
            return;
        }
        
        int consumed = user.getConsumedBenefits().getOrders().get("consumed");
        int total = user.getConsumedBenefits().getOrders().get("total");

        for (LocalDate date = initDay; !date.isAfter(endDay) && !date.isAfter(user.getPlanExpirationDate()); date = date.plusDays(1)) {
            log.info("[process] Init  with : {} {}", user.getUserId(), date);
            OrderEntity orderEntity = orderService.getOrderByDate(user.getUserId(),date);

            log.info("[process] Plan consumed: {}",consumed);
            log.info("[process] Plan Total: {}",total);
            try{
                var principalBenefits = user.getConsumedBenefits().getPrincipalBenefits();
                var defaultMenus = getDefaultMenu(date);
                var principalMenus = getPrincipalMenus(principalBenefits,defaultMenus);

                log.info("[process] Principal Benefits: {}", principalBenefits);
                log.info("[process] Default Menus: {}", defaultMenus);
                log.info("[process] Principal Menu: {}", principalMenus);

                if (orderEntity == null && (consumed+1)<=total) {
                    this.registerOrder(date,deliveryPoint,user,principalMenus);
                    log.info("[process] Register successful  for : {} {}", user.getUserId(), date);
                    consumed++;

                    if (consumed>total &&  user.getCredits()!=null){
                        user.setConsumedBenefits(user.getCredits());
                        user.setCredits(null);
                        userPlanRepository.updateBenefitsAndCredits(user);
                        consumed = user.getConsumedBenefits().getOrders().get("consumed");
                        total = user.getConsumedBenefits().getOrders().get("total");
                    }
                    continue;
                }

                log.info("[process] Already order register for : {} {}", user.getUserId(), date);
            }catch (Exception e){
                log.error("[process] Error with user {}: {} ", user.getUserId(), e.getMessage());
                return;
            }
        }

        log.info("[process] End process for : {}", user);
    }

    /**
     * Obtiene los menús predeterminados disponibles para una fecha específica.
     *
     * @param date Fecha de consulta de menús.
     * @return Mapa con los tipos de menú como claves y sus IDs como valores.
     * @throws BusinessException si no se encuentran menús programados para esa fecha.
     */
    private Map<String, Integer> getDefaultMenu(LocalDate date){
        Map<String, Integer> defaults = menusService.random(date);

        if (defaults == null || defaults.isEmpty()){
            log.error("[getDefaultMenu] No default menus found for date: {}", date);
            throw new BusinessException("No default menus programmed found for date: " + date);
        }

        return defaults;
    }

    /**
     * Filtra los menús predeterminados para obtener solo los que corresponden a los beneficios principales del usuario.
     *
     * @param types         Tipos de beneficios principales del usuario.
     * @param defaultMenus  Menús predeterminados disponibles.
     * @return Lista de IDs de menús asignados.
     * @throws BusinessException si algún beneficio principal no tiene menú asignado.
     */
    private List<Integer> getPrincipalMenus(List<String> types, Map<String, Integer> defaultMenus){
        if (!types.stream().allMatch(defaultMenus::containsKey)){
            log.error("[getPrincipalMenus] Not menu programmed for {} - {}", types, defaultMenus);
            throw new BusinessException("Not menu programmed for " + types);
        }
        List<Integer> menus = new ArrayList<>();
        for (String type : types){
            if (defaultMenus.containsKey(type)) menus.add(defaultMenus.get(type));
        }
        return menus;
    }

    /**
     * Registra una orden en el sistema.
     *
     * @param date            Fecha de la orden.
     * @param deliveryPointId ID del punto de entrega asignado.
     * @param userPlan        Información del plan del usuario.
     * @param principalMenus  Menús asignados a la orden.
     */
    private void registerOrder(LocalDate date, Long deliveryPointId, UserPlanEntity userPlan, List<Integer> principalMenus){
        ScheduleOrderDTO order = new ScheduleOrderDTO(
                date,
                principalMenus,
                null,
                null
        );
        orderService.createOrder(userPlan.getUserId(), deliveryPointId,order,userPlan.getConsumedBenefits());
    }

    /**
     * Calcula la fecha del próximo lunes a partir de una fecha base.
     *
     * @param date Fecha actual.
     * @return Próximo lunes.
     */
    private LocalDate getNextDay(LocalDate date) {
        LocalDate nextDate = date.plusDays(1);
        while (nextDate.getDayOfWeek() != DayOfWeek.MONDAY) {
            nextDate = nextDate.plusDays(1);
        }
        return nextDate;
    }
}
