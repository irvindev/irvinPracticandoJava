package com.pe.allpafood.api.transaction.notification.bussiness.impl;

import com.pe.allpafood.api.transaction.notification.bussiness.INotificationService;
import com.pe.allpafood.api.transaction.notification.entity.email.EmailAddress;
import com.pe.allpafood.api.transaction.notification.entity.email.EmailAttachment;
import com.pe.allpafood.api.transaction.notification.entity.email.EmailMessage;
import jakarta.mail.util.ByteArrayDataSource;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;

@Service
@RequiredArgsConstructor
public class MailNotification implements INotificationService<EmailMessage> {
    private final JavaMailSender mailSender;
    private final MailTemplateService templateService;
    @Value("${app.mail.default-from}")
    private String defaultFrom;
    @Value("${app.mail.default-reply-to:}")
    private String defaultReplyTo;

    @Override
    public void sendNotification(EmailMessage email) {
        mailSender.send(mimeMessage -> {
            MimeMessageHelper helper = new MimeMessageHelper(
                    mimeMessage,
                    !email.getAttachments().isEmpty(),
                    StandardCharsets.UTF_8.name()
            );

            // FROM
            EmailAddress from = email.getFrom();
            if (from != null) {
                helper.setFrom(from.email(), from.name());
            } else {
                helper.setFrom(defaultFrom);
            }

            // REPLY-TO
            EmailAddress replyTo = email.getReplyTo();
            if (replyTo != null) {
                helper.setReplyTo(replyTo.email(), replyTo.name());
            } else if (defaultReplyTo != null && !defaultReplyTo.isBlank()) {
                helper.setReplyTo(defaultReplyTo);
            }

            // TO
            helper.setTo(email.getTo().stream()
                    .map(EmailAddress::email)
                    .toArray(String[]::new));

            // CC
            if (!email.getCc().isEmpty()) {
                helper.setCc(email.getCc().stream()
                        .map(EmailAddress::email)
                        .toArray(String[]::new));
            }

            // BCC
            if (!email.getBcc().isEmpty()) {
                helper.setBcc(email.getBcc().stream()
                        .map(EmailAddress::email)
                        .toArray(String[]::new));
            }

            helper.setSubject(email.getSubject());

            // Body: plantilla o texto directo
            String body;
            boolean isHtml = email.isHtml();

            if (email.getTemplateName() != null && !email.getTemplateName().isBlank()) {
                body = templateService.processTemplate(email.getTemplateName(), email.getVariables());
                isHtml = true; // si usas plantilla HTML
            } else {
                body = email.getBody();
            }

            helper.setText(body, isHtml);

            // Adjuntos
            for (EmailAttachment att : email.getAttachments()) {
                helper.addAttachment(att.fileName(),
                        new ByteArrayDataSource(att.content(), att.contentType()));
            }
        });
    }
}
