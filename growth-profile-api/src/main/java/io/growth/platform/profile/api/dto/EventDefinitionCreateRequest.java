package io.growth.platform.profile.api.dto;

import io.growth.platform.profile.api.enums.EventType;
import io.growth.platform.profile.api.enums.SourceType;
import io.growth.platform.profile.api.enums.UsageChannel;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;
import java.util.Set;

@Data
public class EventDefinitionCreateRequest {

    @NotBlank
    private String eventName;

    @NotNull
    private EventType eventType;

    @NotBlank
    private String displayName;

    private String description;

    private List<PropertyDefinitionDTO> properties;

    private SourceType sourceType;

    @Valid
    private MqSourceConfigDTO mqSourceConfig;

    @NotEmpty
    private Set<UsageChannel> usageChannels;
}
