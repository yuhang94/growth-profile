package io.growth.platform.profile.domain.model;

import io.growth.platform.profile.api.enums.EventType;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class BehaviorEventDefinition {

    private Long id;
    private String eventName;
    private EventType eventType;
    private String displayName;
    private String description;
    private List<PropertyDefinition> properties;
    private Integer status;
    private String createdBy;
    private LocalDateTime createdTime;
    private LocalDateTime updatedTime;

    public boolean isEnabled() {
        return status != null && status == 1;
    }
}
