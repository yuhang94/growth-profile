package io.growth.platform.profile.domain.model;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Segment {

    private Long id;
    private String segmentName;
    private String description;
    private SegmentCondition rootCondition;
    private Integer status;
    private Long lastUserCount;
    private LocalDateTime lastComputedTime;
    private String createdBy;
    private LocalDateTime createdTime;
    private LocalDateTime updatedTime;

    public boolean isEnabled() {
        return status != null && status == 1;
    }
}
