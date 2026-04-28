package io.growth.platform.profile.api.dto;

import io.growth.platform.profile.api.enums.EventType;
import io.growth.platform.profile.api.enums.SourceType;
import io.growth.platform.profile.api.enums.UsageChannel;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Data
public class EventDefinitionDTO {

    private Long id;
    private String eventName;
    private EventType eventType;
    private String displayName;
    private String description;
    private List<PropertyDefinitionDTO> properties;
    private SourceType sourceType;
    private MqSourceConfigDTO mqSourceConfig;
    private Set<UsageChannel> usageChannels;
    private Integer status;
    private String createdBy;
    private LocalDateTime createdTime;
    private LocalDateTime updatedTime;
}
