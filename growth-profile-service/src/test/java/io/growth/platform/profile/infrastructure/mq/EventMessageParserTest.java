package io.growth.platform.profile.infrastructure.mq;

import io.growth.platform.profile.api.dto.FieldMapping;
import io.growth.platform.profile.api.enums.ExtractStrategy;
import io.growth.platform.profile.domain.model.BehaviorEvent;
import io.growth.platform.profile.infrastructure.mq.extract.GroovyExtractor;
import io.growth.platform.profile.infrastructure.mq.extract.JsonPathExtractor;
import io.growth.platform.profile.infrastructure.mq.extract.LocalFuncExtractor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class EventMessageParserTest {

    private EventMessageParser parser;

    @BeforeEach
    void setUp() {
        parser = new EventMessageParser(
                new JsonPathExtractor(),
                new GroovyExtractor(),
                new LocalFuncExtractor()
        );
    }

    @Test
    void parse_fullMessage_withJsonPath() {
        String json = """
                {
                    "uid": "user001",
                    "ts": "2024-01-15 10:30:00",
                    "page": "/home",
                    "duration": 120
                }
                """;

        List<FieldMapping> mappings = List.of(
                newMapping("userId", ExtractStrategy.JSON_PATH, "$.uid", null, null),
                newMapping("eventTime", ExtractStrategy.JSON_PATH, "$.ts", "DATETIME_STRING", null),
                newMapping("page", ExtractStrategy.JSON_PATH, "$.page", null, null),
                newMapping("duration", ExtractStrategy.JSON_PATH, "$.duration", null, null)
        );

        BehaviorEvent event = parser.parse(json, "page_view", mappings);

        assertEquals("user001", event.getUserId());
        assertEquals("page_view", event.getEventName());
        assertNotNull(event.getEventTime());
        assertEquals("/home", event.getProperties().get("page"));
        assertEquals("120", event.getProperties().get("duration"));
    }

    @Test
    void parse_mixedStrategies() {
        String json = """
                {
                    "uid": "user002",
                    "price": 100,
                    "quantity": 3
                }
                """;

        List<FieldMapping> mappings = List.of(
                newMapping("userId", ExtractStrategy.JSON_PATH, "$.uid", null, null),
                newMapping("totalAmount", ExtractStrategy.GROOVY, "msg.price * msg.quantity", null, null)
        );

        BehaviorEvent event = parser.parse(json, "purchase", mappings);

        assertEquals("user002", event.getUserId());
        assertEquals("300", event.getProperties().get("totalAmount"));
    }

    @Test
    void parse_defaultValue_whenExtractionReturnsNull() {
        String json = """
                {"uid": "user003"}
                """;

        List<FieldMapping> mappings = List.of(
                newMapping("userId", ExtractStrategy.JSON_PATH, "$.uid", null, null),
                newMapping("channel", ExtractStrategy.JSON_PATH, "$.channel", null, "organic")
        );

        BehaviorEvent event = parser.parse(json, "login", mappings);

        assertEquals("user003", event.getUserId());
        assertEquals("organic", event.getProperties().get("channel"));
    }

    @Test
    void parse_eventTimeDefaultsToNow_whenNotMapped() {
        String json = """
                {"uid": "user004"}
                """;

        List<FieldMapping> mappings = List.of(
                newMapping("userId", ExtractStrategy.JSON_PATH, "$.uid", null, null)
        );

        BehaviorEvent event = parser.parse(json, "test_event", mappings);

        assertNotNull(event.getEventTime());
    }

    @Test
    void parse_epochSecondEventTime() {
        String json = """
                {"uid": "user005", "timestamp": 1705283400}
                """;

        List<FieldMapping> mappings = List.of(
                newMapping("userId", ExtractStrategy.JSON_PATH, "$.uid", null, null),
                newMapping("eventTime", ExtractStrategy.JSON_PATH, "$.timestamp", "EPOCH_SECOND", null)
        );

        BehaviorEvent event = parser.parse(json, "test_event", mappings);

        assertNotNull(event.getEventTime());
        assertEquals(2024, event.getEventTime().getYear());
    }

    private FieldMapping newMapping(String targetField, ExtractStrategy strategy,
                                    String expression, String sourceType, String defaultValue) {
        FieldMapping mapping = new FieldMapping();
        mapping.setTargetField(targetField);
        mapping.setStrategy(strategy);
        mapping.setExpression(expression);
        mapping.setSourceType(sourceType);
        mapping.setDefaultValue(defaultValue);
        return mapping;
    }
}
