package com.pe.allpafood.api.core.utils.converter;

import java.time.*;

public class TimeUtil {
    private final static String ZONE_ID = "America/Lima";

    public static LocalDateTime getPeruDateTime() {
        ZonedDateTime peruTime = ZonedDateTime.now(ZoneId.of(ZONE_ID));

        return peruTime.toLocalDateTime();
    }

    public static LocalTime getPeruTime() {
        ZonedDateTime peruTime = ZonedDateTime.now(ZoneId.of(ZONE_ID));

        return peruTime.toLocalTime();
    }

    public static LocalDate getPeruDate() {
        ZonedDateTime peruTime = ZonedDateTime.now(ZoneId.of(ZONE_ID));

        return peruTime.toLocalDate();
    }
}
