package io.growth.platform.profile.api.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class EventTemplateUpdateRequest {

    private String templateName;

    private String description;

    @NotBlank
    private String sampleJson;
}
