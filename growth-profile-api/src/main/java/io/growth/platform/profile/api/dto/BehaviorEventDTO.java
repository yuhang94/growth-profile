package io.growth.platform.profile.api.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

@Data
public class BehaviorEventDTO {

    private String eventId;
    private String userId;
    private String eventName;
    private String eventType;
    private Map<String, String> properties;
    private LocalDateTime eventTime;
    private LocalDateTime createdTime;
}
