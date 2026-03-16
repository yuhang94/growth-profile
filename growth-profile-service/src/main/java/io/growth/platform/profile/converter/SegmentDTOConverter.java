package io.growth.platform.profile.converter;

import io.growth.platform.profile.api.dto.SegmentConditionDTO;
import io.growth.platform.profile.api.dto.SegmentCreateRequest;
import io.growth.platform.profile.api.dto.SegmentDTO;
import io.growth.platform.profile.api.dto.SegmentUpdateRequest;
import io.growth.platform.profile.domain.model.Segment;
import io.growth.platform.profile.domain.model.SegmentCondition;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

@Mapper
public interface SegmentDTOConverter {

    SegmentDTOConverter INSTANCE = Mappers.getMapper(SegmentDTOConverter.class);

    @Mapping(target = "rootCondition", source = "rootCondition")
    Segment toDomain(SegmentCreateRequest request);

    void updateDomain(SegmentUpdateRequest request, @MappingTarget Segment domain);

    SegmentDTO toDTO(Segment domain);

    SegmentCondition toConditionDomain(SegmentConditionDTO dto);

    SegmentConditionDTO toConditionDTO(SegmentCondition domain);
}
