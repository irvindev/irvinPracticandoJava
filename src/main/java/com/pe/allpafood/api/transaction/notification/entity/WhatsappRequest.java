package com.pe.allpafood.api.transaction.notification.entity;


import java.util.List;

public record WhatsappRequest (
        List<TemplateRequest.Component> components,
        String templateName,
        String phoneNumber
){

}
