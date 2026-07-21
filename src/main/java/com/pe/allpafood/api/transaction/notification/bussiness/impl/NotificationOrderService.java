package com.pe.allpafood.api.transaction.notification.bussiness.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationOrderService {
    private final SimpMessagingTemplate messagingTemplate;

    @Async
    public void notifyFrontend(LocalDate date) {
        Map<String, Object> payload = Map.of(
                "scheduleDate", date
        );
        try{
            log.info("send notification order {}",payload.toString());
            messagingTemplate.convertAndSend("/topic/orders/notifications", payload);
        }catch (Exception e){
            log.error("error to send order notification {}", e.getMessage());
            throw new RuntimeException("error to create order");
        }
    }
}
