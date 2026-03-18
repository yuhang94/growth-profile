package io.growth.platform.profile.domain.service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

public final class DateTimeParser {

    private DateTimeParser() {
    }

    private static final List<DateTimeFormatter> DATETIME_FORMATTERS = List.of(
            DateTimeFormatter.ISO_LOCAL_DATE_TIME,
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
            DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss"),
            DateTimeFormatter.ofPattern("yyyyMMddHHmmss")
    );

    /**
     * Parse a raw value to LocalDateTime based on sourceType.
     *
     * @param rawValue   the raw value (Long, String, etc.)
     * @param sourceType EPOCH_SECOND, EPOCH_MILLIS, or DATETIME_STRING (default)
     * @return parsed LocalDateTime
     */
    public static LocalDateTime parse(Object rawValue, String sourceType) {
        if (rawValue == null) {
            return null;
        }

        if ("EPOCH_SECOND".equals(sourceType)) {
            long epochSecond = toLong(rawValue);
            return LocalDateTime.ofInstant(Instant.ofEpochSecond(epochSecond), ZoneId.systemDefault());
        }

        if ("EPOCH_MILLIS".equals(sourceType)) {
            long epochMillis = toLong(rawValue);
            return LocalDateTime.ofInstant(Instant.ofEpochMilli(epochMillis), ZoneId.systemDefault());
        }

        // Default: DATETIME_STRING
        String str = rawValue.toString().trim();
        for (DateTimeFormatter formatter : DATETIME_FORMATTERS) {
            try {
                return LocalDateTime.parse(str, formatter);
            } catch (DateTimeParseException ignored) {
                // try next
            }
        }
        throw new IllegalArgumentException("Cannot parse datetime string: " + str);
    }

    private static long toLong(Object value) {
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        return Long.parseLong(value.toString().trim());
    }
}
