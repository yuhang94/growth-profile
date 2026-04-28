package io.growth.platform.profile.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NormalizedBehaviorEvent {

    private String traceId;
    private String eventId;
    private String tenantId;
    private String userId;
    private String eventName;
    private String eventType;
    private LocalDateTime occurredAt;
    private String sourceType;
    private String sourceName;
    private Map<String, String> properties;
}
