package io.growth.platform.profile.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class SegmentTemplateCreateRequest {

    @NotBlank
    private String templateKey;

    @NotBlank
    private String title;

    private String description;

    @NotEmpty
    private List<ConditionSlotDTO> slots;
}
