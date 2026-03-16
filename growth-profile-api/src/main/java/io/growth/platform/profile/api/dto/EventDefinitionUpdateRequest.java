package io.growth.platform.profile.api.dto;

import io.growth.platform.profile.api.enums.EventType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class EventDefinitionUpdateRequest {

    @NotNull
    private EventType eventType;

    @NotBlank
    private String displayName;

    private String description;

    private List<PropertyDefinitionDTO> properties;
}
