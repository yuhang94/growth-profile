package io.growth.platform.profile.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SegmentMatchRequest {

    @NotNull
    private Long segmentId;

    @NotBlank
    private String userId;

    private LocalDateTime contextTime;
}
