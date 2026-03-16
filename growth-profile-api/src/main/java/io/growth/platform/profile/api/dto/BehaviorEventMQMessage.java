package io.growth.platform.profile.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BehaviorEventMQMessage {

    private String userId;
    private String eventName;
    private Map<String, String> properties;
    private LocalDateTime eventTime;
}
