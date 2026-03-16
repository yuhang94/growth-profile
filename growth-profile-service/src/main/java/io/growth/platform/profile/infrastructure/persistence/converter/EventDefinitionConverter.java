package io.growth.platform.profile.infrastructure.persistence.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.growth.platform.profile.api.enums.EventType;
import io.growth.platform.profile.domain.model.BehaviorEventDefinition;
import io.growth.platform.profile.domain.model.PropertyDefinition;
import io.growth.platform.profile.infrastructure.persistence.dataobject.EventDefinitionDO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

import java.util.Collections;
import java.util.List;

@Mapper
public interface EventDefinitionConverter {

    EventDefinitionConverter INSTANCE = Mappers.getMapper(EventDefinitionConverter.class);
    ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Mapping(target = "eventType", source = "eventType", qualifiedByName = "eventTypeToString")
    @Mapping(target = "propertiesJson", source = "properties", qualifiedByName = "propertiesToJson")
    EventDefinitionDO toDataObject(BehaviorEventDefinition domain);

    @Mapping(target = "eventType", source = "eventType", qualifiedByName = "stringToEventType")
    @Mapping(target = "properties", source = "propertiesJson", qualifiedByName = "jsonToProperties")
    BehaviorEventDefinition toDomain(EventDefinitionDO dataObject);

    @Named("eventTypeToString")
    default String eventTypeToString(EventType eventType) {
        return eventType == null ? null : eventType.name();
    }

    @Named("stringToEventType")
    default EventType stringToEventType(String eventType) {
        return eventType == null ? null : EventType.valueOf(eventType);
    }

    @Named("propertiesToJson")
    default String propertiesToJson(List<PropertyDefinition> properties) {
        if (properties == null || properties.isEmpty()) {
            return null;
        }
        try {
            return OBJECT_MAPPER.writeValueAsString(properties);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize properties", e);
        }
    }

    @Named("jsonToProperties")
    default List<PropertyDefinition> jsonToProperties(String json) {
        if (json == null || json.isBlank()) {
            return Collections.emptyList();
        }
        try {
            return OBJECT_MAPPER.readValue(json, new TypeReference<>() {});
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to deserialize properties", e);
        }
    }
}
