package io.growth.platform.profile.infrastructure.persistence.converter;

import io.growth.platform.profile.api.enums.EventType;
import io.growth.platform.profile.domain.model.EventTemplate;
import io.growth.platform.profile.infrastructure.persistence.dataobject.EventTemplateDO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

@Mapper
public interface EventTemplateConverter {

    EventTemplateConverter INSTANCE = Mappers.getMapper(EventTemplateConverter.class);

    @Mapping(target = "eventType", source = "eventType", qualifiedByName = "eventTypeToString")
    EventTemplateDO toDataObject(EventTemplate domain);

    @Mapping(target = "eventType", source = "eventType", qualifiedByName = "stringToEventType")
    EventTemplate toDomain(EventTemplateDO dataObject);

    @Named("eventTypeToString")
    default String eventTypeToString(EventType eventType) {
        return eventType == null ? null : eventType.name();
    }

    @Named("stringToEventType")
    default EventType stringToEventType(String eventType) {
        return eventType == null ? null : EventType.valueOf(eventType);
    }
}
