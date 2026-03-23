package io.growth.platform.profile.converter;

import io.growth.platform.profile.api.dto.EventTemplateCreateRequest;
import io.growth.platform.profile.api.dto.EventTemplateDTO;
import io.growth.platform.profile.api.dto.EventTemplateUpdateRequest;
import io.growth.platform.profile.domain.model.EventTemplate;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

@Mapper
public interface EventTemplateDTOConverter {

    EventTemplateDTOConverter INSTANCE = Mappers.getMapper(EventTemplateDTOConverter.class);

    EventTemplate toDomain(EventTemplateCreateRequest request);

    void updateDomain(EventTemplateUpdateRequest request, @MappingTarget EventTemplate domain);

    EventTemplateDTO toDTO(EventTemplate domain);
}
