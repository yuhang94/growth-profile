package io.growth.platform.profile.converter;

import io.growth.platform.profile.api.dto.EventDefinitionCreateRequest;
import io.growth.platform.profile.api.dto.EventDefinitionDTO;
import io.growth.platform.profile.api.dto.EventDefinitionUpdateRequest;
import io.growth.platform.profile.api.dto.PropertyDefinitionDTO;
import io.growth.platform.profile.domain.model.BehaviorEventDefinition;
import io.growth.platform.profile.domain.model.PropertyDefinition;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

@Mapper
public interface EventDefinitionDTOConverter {

    EventDefinitionDTOConverter INSTANCE = Mappers.getMapper(EventDefinitionDTOConverter.class);

    BehaviorEventDefinition toDomain(EventDefinitionCreateRequest request);

    void updateDomain(EventDefinitionUpdateRequest request, @MappingTarget BehaviorEventDefinition domain);

    EventDefinitionDTO toDTO(BehaviorEventDefinition domain);

    PropertyDefinition toPropertyDomain(PropertyDefinitionDTO dto);

    PropertyDefinitionDTO toPropertyDTO(PropertyDefinition domain);
}
