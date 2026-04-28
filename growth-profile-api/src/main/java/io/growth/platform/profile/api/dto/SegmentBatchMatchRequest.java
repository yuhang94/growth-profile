package io.growth.platform.profile.api.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class SegmentBatchMatchRequest {

    @NotNull
    private Long segmentId;

    @NotEmpty
    private List<String> userIds;

    private LocalDateTime contextTime;
}
