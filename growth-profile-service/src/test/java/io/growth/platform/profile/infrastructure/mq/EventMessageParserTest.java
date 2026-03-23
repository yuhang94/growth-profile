package io.growth.platform.profile.infrastructure.mq;

import io.growth.platform.profile.api.dto.FieldMapping;
import io.growth.platform.profile.api.enums.ExtractStrategy;
import io.growth.platform.profile.domain.model.BehaviorEvent;
import io.growth.platform.profile.domain.model.PropertyDefinition;
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
                newMapping("userId", ExtractStrategy.JSON_PATH, "$.uid"),
                newMapping("eventTime", ExtractStrategy.JSON_PATH, "$.ts"),
                newMapping("page", ExtractStrategy.JSON_PATH, "$.page"),
                newMapping("duration", ExtractStrategy.JSON_PATH, "$.duration")
        );
        List<PropertyDefinition> properties = List.of(
                newProperty("userId", "STRING", null),
                newProperty("eventTime", "DATETIME_STRING", null),
                newProperty("page", "STRING", null),
                newProperty("duration", "LONG", null)
        );

        BehaviorEvent event = parser.parse(json, "page_view", mappings, properties);

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
                newMapping("userId", ExtractStrategy.JSON_PATH, "$.uid"),
                newMapping("totalAmount", ExtractStrategy.GROOVY, "msg.price * msg.quantity")
        );
        List<PropertyDefinition> properties = List.of(
                newProperty("userId", "STRING", null),
                newProperty("totalAmount", "DOUBLE", null)
        );

        BehaviorEvent event = parser.parse(json, "purchase", mappings, properties);

        assertEquals("user002", event.getUserId());
        assertEquals("300", event.getProperties().get("totalAmount"));
    }

    @Test
    void parse_defaultValue_whenExtractionReturnsNull() {
        String json = """
                {"uid": "user003"}
                """;

        List<FieldMapping> mappings = List.of(
                newMapping("userId", ExtractStrategy.JSON_PATH, "$.uid"),
                newMapping("channel", ExtractStrategy.JSON_PATH, "$.channel")
        );
        List<PropertyDefinition> properties = List.of(
                newProperty("userId", "STRING", null),
                newProperty("channel", "STRING", "organic")
        );

        BehaviorEvent event = parser.parse(json, "login", mappings, properties);

        assertEquals("user003", event.getUserId());
        assertEquals("organic", event.getProperties().get("channel"));
    }

    @Test
    void parse_eventTimeDefaultsToNow_whenNotMapped() {
        String json = """
                {"uid": "user004"}
                """;

        List<FieldMapping> mappings = List.of(
                newMapping("userId", ExtractStrategy.JSON_PATH, "$.uid")
        );
        List<PropertyDefinition> properties = List.of(newProperty("userId", "STRING", null));

        BehaviorEvent event = parser.parse(json, "test_event", mappings, properties);

        assertNotNull(event.getEventTime());
    }

    @Test
    void parse_epochSecondEventTime() {
        String json = """
                {"uid": "user005", "timestamp": 1705283400}
                """;

        List<FieldMapping> mappings = List.of(
                newMapping("userId", ExtractStrategy.JSON_PATH, "$.uid"),
                newMapping("eventTime", ExtractStrategy.JSON_PATH, "$.timestamp")
        );
        List<PropertyDefinition> properties = List.of(
                newProperty("userId", "STRING", null),
                newProperty("eventTime", "EPOCH_SECOND", null)
        );

        BehaviorEvent event = parser.parse(json, "test_event", mappings, properties);

        assertNotNull(event.getEventTime());
        assertEquals(2024, event.getEventTime().getYear());
    }

    private FieldMapping newMapping(String targetField, ExtractStrategy strategy, String expression) {
        FieldMapping mapping = new FieldMapping();
        mapping.setTargetField(targetField);
        mapping.setStrategy(strategy);
        mapping.setExpression(expression);
        return mapping;
    }

    private PropertyDefinition newProperty(String propertyName, String propertyType, String defaultValue) {
        PropertyDefinition property = new PropertyDefinition();
        property.setPropertyName(propertyName);
        property.setPropertyType(propertyType);
        property.setDefaultValue(defaultValue);
        return property;
    }
}
