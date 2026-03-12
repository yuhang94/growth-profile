package io.growth.platform.profile.converter;

import io.growth.platform.profile.api.dto.TagDefinitionCreateRequest;
import io.growth.platform.profile.api.dto.TagDefinitionDTO;
import io.growth.platform.profile.api.dto.TagDefinitionUpdateRequest;
import io.growth.platform.profile.domain.model.TagDefinition;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

@Mapper
public interface TagDefinitionDTOConverter {

    TagDefinitionDTOConverter INSTANCE = Mappers.getMapper(TagDefinitionDTOConverter.class);

    TagDefinition toDomain(TagDefinitionCreateRequest request);

    void updateDomain(TagDefinitionUpdateRequest request, @MappingTarget TagDefinition domain);

    TagDefinitionDTO toDTO(TagDefinition domain);
}
