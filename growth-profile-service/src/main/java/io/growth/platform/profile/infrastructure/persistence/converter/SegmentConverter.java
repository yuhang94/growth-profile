package io.growth.platform.profile.infrastructure.persistence.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.growth.platform.profile.domain.model.Segment;
import io.growth.platform.profile.domain.model.SegmentCondition;
import io.growth.platform.profile.infrastructure.persistence.dataobject.SegmentDO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

@Mapper
public interface SegmentConverter {

    SegmentConverter INSTANCE = Mappers.getMapper(SegmentConverter.class);
    ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Mapping(target = "conditionJson", source = "rootCondition", qualifiedByName = "conditionToJson")
    SegmentDO toDataObject(Segment domain);

    @Mapping(target = "rootCondition", source = "conditionJson", qualifiedByName = "jsonToCondition")
    Segment toDomain(SegmentDO dataObject);

    @Named("conditionToJson")
    default String conditionToJson(SegmentCondition condition) {
        if (condition == null) {
            return null;
        }
        try {
            return OBJECT_MAPPER.writeValueAsString(condition);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize segment condition", e);
        }
    }

    @Named("jsonToCondition")
    default SegmentCondition jsonToCondition(String json) {
        if (json == null || json.isBlank()) {
            return null;
        }
        try {
            return OBJECT_MAPPER.readValue(json, SegmentCondition.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to deserialize segment condition", e);
        }
    }
}
