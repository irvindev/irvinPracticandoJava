package com.pe.allpafood.api.transaction.notification.utils;

import com.pe.allpafood.api.transaction.notification.entity.TemplateRequest;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class MessageTemplateUtils {

    public static List<TemplateRequest.Component> getDefaultTemplate(
            List<String> itemsHeader,
            List<String> itemsBody
    ) {
        TemplateRequest.Component header = buildHeader(itemsHeader);
        TemplateRequest.Component body = buildBody(itemsBody);

        var components = List.of(header, body);
        log.info("[getComponentsTemplate] components: {}", components);

        return components;
    }

    private static TemplateRequest.Component buildHeader(List<String> itemsHeader) {
        TemplateRequest.Component header = new TemplateRequest.Component();
        header.setType("header");
        header.setParameters(itemsHeader.stream()
                .map(MessageTemplateUtils::buildTextParam)  // o MethodReference: TemplateRequestBuilder::buildTextParam
                .toList());

        return header;
    }

    private static TemplateRequest.Component buildBody(List<String> itemsBody) {
        TemplateRequest.Component body = new TemplateRequest.Component();
        body.setType("body");
        body.setParameters(itemsBody.stream()
                .map(MessageTemplateUtils::buildTextParam)  // o MethodReference: TemplateRequestBuilder::buildTextParam
                .toList());

        return body;
    }

    private static TemplateRequest.Parameter buildTextParam(String value) {
        TemplateRequest.Parameter parameter = new TemplateRequest.Parameter();
        parameter.setType("text");
        parameter.setText(value);
        return parameter;
    }
}
