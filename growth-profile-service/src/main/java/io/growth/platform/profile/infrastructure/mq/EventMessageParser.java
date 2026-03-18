package io.growth.platform.profile.infrastructure.mq;

import io.growth.platform.profile.api.dto.FieldMapping;
import io.growth.platform.profile.api.enums.ExtractStrategy;
import io.growth.platform.profile.domain.model.BehaviorEvent;
import io.growth.platform.profile.domain.service.DateTimeParser;
import io.growth.platform.profile.infrastructure.mq.extract.FieldExtractor;
import io.growth.platform.profile.infrastructure.mq.extract.GroovyExtractor;
import io.growth.platform.profile.infrastructure.mq.extract.JsonPathExtractor;
import io.growth.platform.profile.infrastructure.mq.extract.LocalFuncExtractor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
public class EventMessageParser {

    private static final Logger log = LoggerFactory.getLogger(EventMessageParser.class);

    private static final String FIELD_USER_ID = "userId";
    private static final String FIELD_EVENT_TIME = "eventTime";

    private final Map<ExtractStrategy, FieldExtractor> extractors;

    public EventMessageParser(JsonPathExtractor jsonPathExtractor,
                              GroovyExtractor groovyExtractor,
                              LocalFuncExtractor localFuncExtractor) {
        this.extractors = Map.of(
                ExtractStrategy.JSON_PATH, jsonPathExtractor,
                ExtractStrategy.GROOVY, groovyExtractor,
                ExtractStrategy.LOCAL_FUNC, localFuncExtractor
        );
    }

    /**
     * Parse a raw JSON message into a BehaviorEvent using field mappings.
     */
    public BehaviorEvent parse(String rawJson, String eventName, List<FieldMapping> fieldMappings) {
        BehaviorEvent event = new BehaviorEvent();
        event.setEventId(UUID.randomUUID().toString());
        event.setEventName(eventName);
        event.setCreatedTime(LocalDateTime.now());

        Map<String, String> properties = new HashMap<>();

        for (FieldMapping mapping : fieldMappings) {
            Object value = extractValue(rawJson, mapping);

            if (value == null && mapping.getDefaultValue() != null) {
                value = mapping.getDefaultValue();
            }

            if (value == null) {
                continue;
            }

            String targetField = mapping.getTargetField();

            switch (targetField) {
                case FIELD_USER_ID -> event.setUserId(value.toString());
                case FIELD_EVENT_TIME -> {
                    LocalDateTime eventTime = DateTimeParser.parse(value, mapping.getSourceType());
                    event.setEventTime(eventTime);
                }
                default -> properties.put(targetField, value.toString());
            }
        }

        event.setProperties(properties);

        // Default eventTime to now if not mapped
        if (event.getEventTime() == null) {
            event.setEventTime(LocalDateTime.now());
        }

        return event;
    }

    /**
     * Extract a single field value for testing purposes.
     * Returns the raw extracted value (or default).
     */
    public Object extractSingleField(String rawJson, FieldMapping mapping) {
        Object value = extractValue(rawJson, mapping);
        if (value == null && mapping.getDefaultValue() != null) {
            value = mapping.getDefaultValue();
        }
        return value;
    }

    private Object extractValue(String rawJson, FieldMapping mapping) {
        FieldExtractor extractor = extractors.get(mapping.getStrategy());
        if (extractor == null) {
            log.warn("No extractor found for strategy: {}", mapping.getStrategy());
            return null;
        }
        try {
            return extractor.extract(rawJson, mapping.getExpression());
        } catch (Exception e) {
            log.warn("Failed to extract field {}: {}", mapping.getTargetField(), e.getMessage());
            return null;
        }
    }
}
