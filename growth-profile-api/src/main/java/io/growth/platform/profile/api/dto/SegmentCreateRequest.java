package io.growth.platform.profile.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SegmentCreateRequest {

    @NotBlank
    private String segmentName;

    private String description;

    @NotNull
    private SegmentConditionDTO rootCondition;
}
