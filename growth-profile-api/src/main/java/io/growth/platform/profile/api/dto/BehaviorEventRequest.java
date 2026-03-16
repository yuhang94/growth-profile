package io.growth.platform.profile.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

@Data
public class BehaviorEventRequest {

    @NotBlank
    private String userId;

    @NotBlank
    private String eventName;

    private Map<String, String> properties;

    @NotNull
    private LocalDateTime eventTime;
}
