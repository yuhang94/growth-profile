package io.growth.platform.profile.domain.service;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class DateTimeParserTest {

    @Test
    void parse_epochSecond() {
        // 2024-01-15 10:30:00 UTC+8 = 1705283400
        LocalDateTime result = DateTimeParser.parse(1705283400L, "EPOCH_SECOND");
        assertNotNull(result);
        assertEquals(2024, result.getYear());
    }

    @Test
    void parse_epochSecond_fromString() {
        LocalDateTime result = DateTimeParser.parse("1705283400", "EPOCH_SECOND");
        assertNotNull(result);
        assertEquals(2024, result.getYear());
    }

    @Test
    void parse_epochMillis() {
        LocalDateTime result = DateTimeParser.parse(1705283400000L, "EPOCH_MILLIS");
        assertNotNull(result);
        assertEquals(2024, result.getYear());
    }

    @Test
    void parse_datetimeString_iso() {
        LocalDateTime result = DateTimeParser.parse("2024-01-15T10:30:00", "DATETIME_STRING");
        assertNotNull(result);
        assertEquals(2024, result.getYear());
        assertEquals(1, result.getMonthValue());
        assertEquals(15, result.getDayOfMonth());
        assertEquals(10, result.getHour());
        assertEquals(30, result.getMinute());
    }

    @Test
    void parse_datetimeString_standard() {
        LocalDateTime result = DateTimeParser.parse("2024-01-15 10:30:00", "DATETIME_STRING");
        assertNotNull(result);
        assertEquals(2024, result.getYear());
        assertEquals(10, result.getHour());
    }

    @Test
    void parse_datetimeString_slash() {
        LocalDateTime result = DateTimeParser.parse("2024/01/15 10:30:00", "DATETIME_STRING");
        assertNotNull(result);
        assertEquals(2024, result.getYear());
    }

    @Test
    void parse_datetimeString_compact() {
        LocalDateTime result = DateTimeParser.parse("20240115103000", "DATETIME_STRING");
        assertNotNull(result);
        assertEquals(2024, result.getYear());
        assertEquals(10, result.getHour());
    }

    @Test
    void parse_null_returnsNull() {
        assertNull(DateTimeParser.parse(null, "EPOCH_SECOND"));
    }

    @Test
    void parse_invalidDatetime_throwsException() {
        assertThrows(IllegalArgumentException.class,
                () -> DateTimeParser.parse("not-a-date", "DATETIME_STRING"));
    }

    @Test
    void parse_defaultSourceType() {
        // null sourceType defaults to DATETIME_STRING
        LocalDateTime result = DateTimeParser.parse("2024-01-15T10:30:00", null);
        assertNotNull(result);
        assertEquals(2024, result.getYear());
    }
}
