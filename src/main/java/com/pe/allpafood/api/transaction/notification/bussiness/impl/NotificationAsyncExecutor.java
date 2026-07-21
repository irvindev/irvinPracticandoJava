package com.pe.allpafood.api.transaction.notification.bussiness.impl;

import com.pe.allpafood.api.transaction.notification.entity.WhatsappRequest;
import com.pe.allpafood.api.transaction.notification.entity.email.EmailMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class NotificationAsyncExecutor {

    private final MailNotification mail;
    private final WhatsappNotification whatsapp;

    public NotificationAsyncExecutor(
            MailNotification mail,
            WhatsappNotification whatsapp) {
        this.mail = mail;
        this.whatsapp = whatsapp;
    }

    @Async
    public void sendNotification(EmailMessage email) {
        try {
            mail.sendNotification(email);

        }catch (Exception e){
            e.printStackTrace();
            log.error("Error to send mail :{}", e.getMessage());
        }
    }

    @Async
    public void sendNotification(WhatsappRequest whatsappMsg) {
        try {
        whatsapp.sendNotification(whatsappMsg);
        }catch (Exception e){
            log.error("Error to send message :{}", e.getMessage());
        }
    }
}
