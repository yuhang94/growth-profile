package io.growth.platform.profile.infrastructure.persistence.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.growth.platform.profile.api.enums.EventType;
import io.growth.platform.profile.api.enums.SourceType;
import io.growth.platform.profile.domain.model.BehaviorEventDefinition;
import io.growth.platform.profile.domain.model.MqSourceConfig;
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
    @Mapping(target = "sourceType", source = "sourceType", qualifiedByName = "sourceTypeToString")
    @Mapping(target = "mqSourceConfigJson", source = "mqSourceConfig", qualifiedByName = "mqSourceConfigToJson")
    EventDefinitionDO toDataObject(BehaviorEventDefinition domain);

    @Mapping(target = "eventType", source = "eventType", qualifiedByName = "stringToEventType")
    @Mapping(target = "properties", source = "propertiesJson", qualifiedByName = "jsonToProperties")
    @Mapping(target = "sourceType", source = "sourceType", qualifiedByName = "stringToSourceType")
    @Mapping(target = "mqSourceConfig", source = "mqSourceConfigJson", qualifiedByName = "jsonToMqSourceConfig")
    BehaviorEventDefinition toDomain(EventDefinitionDO dataObject);

    @Named("eventTypeToString")
    default String eventTypeToString(EventType eventType) {
        return eventType == null ? null : eventType.name();
    }

    @Named("stringToEventType")
    default EventType stringToEventType(String eventType) {
        return eventType == null ? null : EventType.valueOf(eventType);
    }

    @Named("sourceTypeToString")
    default String sourceTypeToString(SourceType sourceType) {
        return sourceType == null ? null : sourceType.name();
    }

    @Named("stringToSourceType")
    default SourceType stringToSourceType(String sourceType) {
        return sourceType == null ? null : SourceType.valueOf(sourceType);
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

    @Named("mqSourceConfigToJson")
    default String mqSourceConfigToJson(MqSourceConfig config) {
        if (config == null) {
            return null;
        }
        try {
            return OBJECT_MAPPER.writeValueAsString(config);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize MqSourceConfig", e);
        }
    }

    @Named("jsonToMqSourceConfig")
    default MqSourceConfig jsonToMqSourceConfig(String json) {
        if (json == null || json.isBlank()) {
            return null;
        }
        try {
            return OBJECT_MAPPER.readValue(json, MqSourceConfig.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to deserialize MqSourceConfig", e);
        }
    }
}
