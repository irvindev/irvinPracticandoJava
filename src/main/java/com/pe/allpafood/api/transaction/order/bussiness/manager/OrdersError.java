package com.pe.allpafood.api.transaction.order.bussiness.manager;

import lombok.Getter;

@Getter
public enum OrdersError {
    DELIVERY_POINT_NOT_SELECTED("El usuario no ha seleccionado un lugar de entrega."),
    DELIVERY_POINT_NOT_EXIST("El sitio del delivery no existe."),
    PLAN_EXPIRATION_DATE_EXCEEDED("Ha superado la fecha límite de su plan."),
    INVALID_SCHEDULE_DATE("Fecha de solicitud no permitida."),
    BENEFITS_LIMIT_EXCEEDED("Ha superado el límite de beneficios de su plan."),
    MENUS_NOT_AVAILABLE("Los menús seleccionados no están disponibles por el momento."),
    ALREADY_EXISTS("Y se ha creado una orden para este día.");

    private final String message;

    OrdersError(String message) {
        this.message = message;
    }
}
