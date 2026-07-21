package com.pe.allpafood.api.transaction.notification.scheduler;

import com.pe.allpafood.api.transaction.notification.bussiness.INotificationService;
import com.pe.allpafood.api.transaction.notification.entity.WhatsappRequest;
import com.pe.allpafood.api.transaction.notification.utils.MessageTemplateUtils;
import com.pe.allpafood.api.transaction.order.repository.IOrderRepository;
import com.pe.allpafood.api.transaction.plan.entities.UserPlanEntity;
import com.pe.allpafood.api.transaction.plan.repository.IUserPlanRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;


@Component
@Slf4j
@RequiredArgsConstructor
public class NotificationProcessor {
    @Value("${notification.service.whatsapp.template-comanda-disponible}")
    private String templateComanda;

    @Value("${application.business.domain.app}")
    private String applicationDomain;

    @Value("${notification.service.whatsapp.template-plan-revenue}")
    private String templatePlanRevenue;

    @Value("${notification.service.whatsapp.template-plan-finalized}")
    private String templatePlanFinalized;

    private final IUserPlanRepository userPlanRepository;
    private final INotificationService<WhatsappRequest> notificationService;


    @Scheduled(cron = "${schedule.notifications.comanda_disponible}", zone = "America/Lima")
    public void scheduleTaskAvailableComanda() {
        log.info("[scheduleTaskCreateDefaultOrders] Starting process}");

        var now = LocalDate.now();
        List<UserPlanEntity> users = userPlanRepository.findAllDataByMajorDate(now);
        for (UserPlanEntity user : users) {
            log.info("[scheduleTaskCreateDefaultOrders] Send to userId: {} ",user.getUserId());

            LocalDate tomorrow = now.plusDays(1);

            if (tomorrow.getDayOfWeek() == DayOfWeek.SUNDAY) {
                log.info("[scheduleTaskCreateDefaultOrders]  Not send message for user, plan expire tomorrow for userId: {}",user.getUserId());
                continue;
            }

            notificationService.sendNotification(new WhatsappRequest(MessageTemplateUtils.getDefaultTemplate(
                    new ArrayList<>(),
                    new ArrayList<>()
            ),this.templateComanda,user.getUser().getPhoneNumber()));
            log.info("[scheduleTaskCreateDefaultOrders] Finish send to userId: {} ",user.getUserId());
        }
        log.info("[scheduleTaskCreateDefaultOrders] Ending process");
    }

    @Scheduled(cron = "${schedule.notifications.plan-revenue}", zone = "America/Lima")
    public void scheduleTaskPlanRevenue() {
        log.info("[scheduleTaskPlanRevenue] Starting process}");

        LocalDate now = LocalDate.now();
        LocalDate end = now.plusDays(2);
        List<UserPlanEntity> users = userPlanRepository.findAllDataByRangeDate(now,end);
        for (UserPlanEntity user : users) {
            log.info("[scheduleTaskPlanRevenue] Send to userId: {} ",user.getUserId());
            var total = user.getConsumedBenefits().getOrders().get("total");
            var consumed =  user.getConsumedBenefits().getOrders().get("consumed");
            var dif = total - consumed;
            if (dif<=2){
                notificationService.sendNotification(new WhatsappRequest(MessageTemplateUtils.getDefaultTemplate(
                        new ArrayList<>(),
                        List.of(String.valueOf(dif))
                ),this.templatePlanRevenue,user.getUser().getPhoneNumber()));
                log.info("[scheduleTaskPlanRevenue] Enter send: {} ",user.getUserId());
            }
            log.info("[scheduleTaskPlanRevenue] Finish send to userId: {} ",user.getUserId());
        }
        log.info("[scheduleTaskPlanRevenue] Ending process");
    }

    @Scheduled(cron = "${schedule.notifications.plan-finalized}", zone = "America/Lima")
    public void scheduleTaskPlanFinalized() {
        log.info("[scheduleTaskPlanFinalized] Starting process}");

        LocalDate now = LocalDate.now().minusDays(1);
        LocalDate end = now.plusDays(2);

        List<UserPlanEntity> users = userPlanRepository.findAllDataByMajorDate(now);
        for (UserPlanEntity user : users) {
            log.info("[scheduleTaskPlanFinalized] Send to userId: {} ",user.getUserId());
            var total = user.getConsumedBenefits().getOrders().get("total");
            var consumed =  user.getConsumedBenefits().getOrders().get("consumed");
            var dif = total - consumed;

            if (dif==0 || user.getPlanExpirationDate().equals(LocalDate.now())){
                notificationService.sendNotification(new WhatsappRequest(MessageTemplateUtils.getDefaultTemplate(
                        new ArrayList<>(),
                        new ArrayList<>()
                ),this.templatePlanFinalized,user.getUser().getPhoneNumber()));
            } else if (user.getPlanExpirationDate().equals(end) || dif <= 2){
                notificationService.sendNotification(new WhatsappRequest(MessageTemplateUtils.getDefaultTemplate(
                        new ArrayList<>(),
                        List.of(String.valueOf(dif))
                ),this.templatePlanRevenue,user.getUser().getPhoneNumber()));
                log.info("[scheduleTaskPlanRevenue] Enter send: {} ",user.getUserId());
            }

            log.info("[scheduleTaskPlanFinalized] Finish send to userId: {} ",user.getUserId());
        }
        log.info("[scheduleTaskPlanFinalized] Ending process");
    }

}
