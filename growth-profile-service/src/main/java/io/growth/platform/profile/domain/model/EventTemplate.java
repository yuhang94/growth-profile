package io.growth.platform.profile.domain.model;

import io.growth.platform.profile.api.enums.EventType;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class EventTemplate {

    private Long id;
    private EventType eventType;
    private String templateName;
    private String description;
    private String sampleJson;
    private String createdBy;
    private LocalDateTime createdTime;
    private LocalDateTime updatedTime;
}
