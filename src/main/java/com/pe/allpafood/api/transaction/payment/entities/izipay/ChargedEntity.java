package com.pe.allpafood.api.transaction.payment.entities.izipay;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;
import java.util.Map;

@Setter
@Getter
@ToString
public class ChargedEntity {

    private String webService;
    private String version;
    private String applicationVersion;
    private String status;
    private PaymentFormAnswer answer;
    private String ticket;
    private String serverDate;
    private String applicationProvider;
    private Object metadata; // Could be a specific type if known
    private String mode;
    private String serverUrl;
    private String type;

    @Getter
    @Setter
    @ToString
    public static class PaymentFormAnswer {
        private String formToken;
        private RiskAnalyzer riskAnalyzer;
        private Categories categories;
        private Map<String, CardDetails> cards;  // Use a Map for dynamic card types
        private SmartForm smartForm;
        private String apiRestVersion;
        private Support support;
        private String country;
        private String jSessionId;
        private String type;
        private String errorCode;
        private String errorMessage;


        @Getter
        @Setter
        @ToString
        public static class RiskAnalyzer {
            private String fingerprintsId;
            private String jsUrl;
        }

        @Getter
        @Setter
        @ToString
        public static class Categories {
            private DebitCreditCards debitCreditCards;

            @Getter
            @Setter
            @ToString
            public static class DebitCreditCards {
                private String appId;
                private List<String> param;

            }
        }

        @Getter
        @Setter
        public static class CardDetails {
            private Map<String, FieldDetails> fields;
            private String copyFrom;

            @Getter
            @Setter
            public static class FieldDetails {
                private Integer maxLength; // For securityCode
                private Integer minLength;  // For pan
                private String value;
                private List<String> values; // For installmentNumber
                private List<String> validators; // For email
                private Boolean required;
                private Boolean sensitive;
                private Boolean hidden;
                private Boolean clearOnError;

            }
        }

        @Getter
        @Setter
        public static class SmartForm {
            private CardsInfo CARDS;

            @Getter
            @Setter
            public static class CardsInfo {
                private Boolean allowIframe;
                private Integer rank;
                private Boolean deadEndPaymentMethod;
                private Boolean newPaymentRequired;
                private Boolean wallet;
            }
        }

        @Getter
        @Setter
        @ToString
        public static class Support {
            private String mail;
        }
    }
}