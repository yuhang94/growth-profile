package io.growth.platform.profile.converter;

import io.growth.platform.profile.api.dto.BehaviorEventDTO;
import io.growth.platform.profile.domain.model.BehaviorEvent;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface BehaviorEventDTOConverter {

    BehaviorEventDTOConverter INSTANCE = Mappers.getMapper(BehaviorEventDTOConverter.class);

    BehaviorEventDTO toDTO(BehaviorEvent domain);
}
