package io.growth.platform.profile.api.dto;

import io.growth.platform.profile.api.enums.EventType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class EventTemplateCreateRequest {

    @NotNull
    private EventType eventType;

    @NotBlank
    private String templateName;

    private String description;

    @NotBlank
    private String sampleJson;
}
