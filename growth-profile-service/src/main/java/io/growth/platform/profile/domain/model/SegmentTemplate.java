package io.growth.platform.profile.domain.model;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class SegmentTemplate {

    private Long id;
    private String templateKey;
    private String title;
    private String description;
    private List<ConditionSlot> slots;
    private int sortOrder;
    private boolean builtIn;
    private LocalDateTime createdTime;
    private LocalDateTime updatedTime;
}
