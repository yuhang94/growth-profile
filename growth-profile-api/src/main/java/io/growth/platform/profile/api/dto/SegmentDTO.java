package io.growth.platform.profile.api.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SegmentDTO {

    private Long id;
    private String segmentName;
    private String description;
    private SegmentConditionDTO rootCondition;
    private Integer status;
    private Long lastUserCount;
    private LocalDateTime lastComputedTime;
    private String createdBy;
    private LocalDateTime createdTime;
    private LocalDateTime updatedTime;
}
