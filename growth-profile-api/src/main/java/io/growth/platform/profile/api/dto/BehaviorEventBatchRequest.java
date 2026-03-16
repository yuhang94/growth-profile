package io.growth.platform.profile.api.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class BehaviorEventBatchRequest {

    @NotEmpty
    @Valid
    private List<BehaviorEventRequest> events;
}
