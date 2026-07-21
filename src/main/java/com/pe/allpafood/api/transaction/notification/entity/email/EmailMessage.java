package com.pe.allpafood.api.transaction.notification.entity.email;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class EmailMessage {

    // Destinatarios
    private List<EmailAddress> to = new ArrayList<>();
    private List<EmailAddress> cc = new ArrayList<>();
    private List<EmailAddress> bcc = new ArrayList<>();

    // Info básica
    private String subject;

    // Opción 1: cuerpo directo
    private String body;
    private boolean html;

    // Opción 2: usar plantilla
    private String templateName;               // p.e. "ticket-confirmation"
    private Map<String, Object> variables;     // p.e. {"userName": "Piero", "eventName": "Lim-On Fest"}

    // Adjuntos
    private List<EmailAttachment> attachments = new ArrayList<>();

    // From / ReplyTo
    private EmailAddress from;
    private EmailAddress replyTo;

    // getters/setters...
}