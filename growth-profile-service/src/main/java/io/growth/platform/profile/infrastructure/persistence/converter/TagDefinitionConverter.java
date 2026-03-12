package io.growth.platform.profile.infrastructure.persistence.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.growth.platform.profile.api.enums.TagType;
import io.growth.platform.profile.domain.model.TagDefinition;
import io.growth.platform.profile.infrastructure.persistence.dataobject.TagDefinitionDO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

import java.util.Collections;
import java.util.List;

@Mapper
public interface TagDefinitionConverter {

    TagDefinitionConverter INSTANCE = Mappers.getMapper(TagDefinitionConverter.class);

    ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Mapping(target = "tagType", source = "tagType", qualifiedByName = "tagTypeToString")
    @Mapping(target = "enumValues", source = "enumValues", qualifiedByName = "listToJson")
    TagDefinitionDO toDataObject(TagDefinition domain);

    @Mapping(target = "tagType", source = "tagType", qualifiedByName = "stringToTagType")
    @Mapping(target = "enumValues", source = "enumValues", qualifiedByName = "jsonToList")
    TagDefinition toDomain(TagDefinitionDO dataObject);

    @Named("tagTypeToString")
    default String tagTypeToString(TagType tagType) {
        return tagType == null ? null : tagType.name();
    }

    @Named("stringToTagType")
    default TagType stringToTagType(String tagType) {
        return tagType == null ? null : TagType.valueOf(tagType);
    }

    @Named("listToJson")
    default String listToJson(List<String> list) {
        if (list == null || list.isEmpty()) {
            return null;
        }
        try {
            return OBJECT_MAPPER.writeValueAsString(list);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize enum values", e);
        }
    }

    @Named("jsonToList")
    default List<String> jsonToList(String json) {
        if (json == null || json.isBlank()) {
            return Collections.emptyList();
        }
        try {
            return OBJECT_MAPPER.readValue(json, new TypeReference<>() {});
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to deserialize enum values", e);
        }
    }
}
