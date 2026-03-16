package io.growth.platform.profile.api.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SegmentPreviewRequest {

    @NotNull
    private SegmentConditionDTO rootCondition;
}
