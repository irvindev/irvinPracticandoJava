package com.pe.allpafood.api.transaction.notification.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Setter
@Getter
public class TemplateRequest {

    // Getters and setters
    @JsonProperty("messaging_product")
    private String messagingProduct;

    @JsonProperty("to")
    private String to;

    @JsonProperty("type")
    private String type;

    @JsonProperty("template")
    private Template template;

    @Setter
    @Getter
    public static class Template {

        // Getters and setters
        @JsonProperty("name")
        private String name;

        @JsonProperty("language")
        private Language language;

        @JsonProperty("components")
        private List<Component> components;

    }

    @Setter
    @Getter
    public static class Language {

        // Getters and setters
        @JsonProperty("code")
        private String code;

    }

    @Setter
    @Getter
    @ToString
    public static class Component {

        // Getters and setters
        @JsonProperty("type")
        private String type;

        @JsonProperty("parameters")
        private List<Parameter> parameters;

        @JsonProperty("sub_type")
        private String subType;

        @JsonProperty("index")
        private Integer index;

    }

    @Setter
    @Getter
    @ToString
    public static class Parameter {

        // Getters and setters
        @JsonProperty("type")
        private String type;

        @JsonProperty("text")
        private String text;

    }
}

