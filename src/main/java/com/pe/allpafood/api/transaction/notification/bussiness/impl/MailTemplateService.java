package com.pe.allpafood.api.transaction.notification.bussiness.impl;

import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.util.Map;

@Service
public class MailTemplateService {

    private final SpringTemplateEngine templateEngine;

    public MailTemplateService(SpringTemplateEngine templateEngine) {
        this.templateEngine = templateEngine;
    }

    public String processTemplate(String templateName, Map<String, Object> variables) {
        Context context = new Context();
        if (variables != null) {
            variables.forEach(context::setVariable);
        }
        return templateEngine.process(templateName, context);
    }
}
