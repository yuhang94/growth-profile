package io.growth.platform.profile.converter;

import io.growth.platform.profile.api.dto.ConditionSlotDTO;
import io.growth.platform.profile.api.dto.SegmentTemplateCreateRequest;
import io.growth.platform.profile.api.dto.SegmentTemplateDTO;
import io.growth.platform.profile.domain.model.ConditionSlot;
import io.growth.platform.profile.domain.model.SegmentTemplate;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface SegmentTemplateDTOConverter {

    SegmentTemplateDTOConverter INSTANCE = Mappers.getMapper(SegmentTemplateDTOConverter.class);

    SegmentTemplate toDomain(SegmentTemplateCreateRequest request);

    SegmentTemplateDTO toDTO(SegmentTemplate domain);

    ConditionSlot toSlotDomain(ConditionSlotDTO dto);

    ConditionSlotDTO toSlotDTO(ConditionSlot domain);
}
