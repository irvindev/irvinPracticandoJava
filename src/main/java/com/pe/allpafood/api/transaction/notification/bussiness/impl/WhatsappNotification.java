package com.pe.allpafood.api.transaction.notification.bussiness.impl;
import com.pe.allpafood.api.transaction.notification.entity.TemplateRequest;
import com.pe.allpafood.api.transaction.notification.entity.WhatsappRequest;
import com.pe.allpafood.api.transaction.notification.bussiness.INotificationService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class WhatsappNotification implements INotificationService<WhatsappRequest> {

    private final RestTemplate restTemplate;

    private HttpHeaders headers;

    @Value("${notification.service.whatsapp.token}")
    private String whatsappAccessToken;

    @Value("${notification.service.whatsapp.url}")
    private String whatsappUrl;

    @Value("${notification.service.whatsapp.messaging-product}")
    private String messagingProduct;

    @Value("${notification.service.whatsapp.template}")
    private String template;

    @Value("${notification.service.whatsapp.code}")
    private String code;

    @PostConstruct
    void init(){
        this.headers = new HttpHeaders();
        this.headers.set("Authorization", "Bearer ".concat(whatsappAccessToken));
        this.headers.set("Content-Type", "application/json");
    }

    @Override
    public void sendNotification(WhatsappRequest request) {
        log.info("[sendMessageTemplate] Starting send message: {}", request);
        HttpEntity<TemplateRequest> entity = createHttpEntity(request.phoneNumber(), request.components(), request.templateName());
        restTemplate.exchange(this.whatsappUrl, HttpMethod.POST, entity, String.class);
        log.info("[sendMessageTemplate] Terminate successful send message: {}", request.phoneNumber());
    }

    private HttpEntity<TemplateRequest> createHttpEntity(String phoneNumber, List<TemplateRequest.Component> components, String templateName){
        TemplateRequest request = new TemplateRequest();
        setBaseRequest(request,phoneNumber);
        setTemplate(request,templateName,components);
        return new HttpEntity<>(request,this.headers);
    }

    private void setBaseRequest(TemplateRequest request, String phoneNumber) {
        request.setMessagingProduct(this.messagingProduct);
        request.setTo(phoneNumber);
        request.setType(this.template);
    }

    private void setTemplate(TemplateRequest request, String name, List<TemplateRequest.Component> components){
        TemplateRequest.Language langBody = new TemplateRequest.Language();
        langBody.setCode(this.code);

        TemplateRequest.Template templateBody = new TemplateRequest.Template();
        templateBody.setName(name);
        templateBody.setLanguage(langBody);

        if(components !=null && !components.isEmpty()) templateBody.setComponents(components);

        request.setTemplate(templateBody);
    }
}
