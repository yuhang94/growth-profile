package io.growth.platform.profile.api.dto;

import io.growth.platform.profile.api.enums.BehaviorOperator;
import io.growth.platform.profile.api.enums.CompareOperator;
import io.growth.platform.profile.api.enums.ConditionOperator;
import lombok.Data;

import java.util.List;

@Data
public class SegmentConditionDTO {

    private ConditionOperator operator;
    private List<SegmentConditionDTO> children;

    // --- 标签条件字段 ---
    private String tagKey;
    private CompareOperator compareOp;
    private List<String> values;

    // --- 行为条件字段 ---
    private BehaviorOperator behaviorOp;
    private String eventName;
    private List<EventPropertyFilterDTO> propertyFilters;
    private String timeRangeStart;
    private String timeRangeEnd;
    private CompareOperator countOp;
    private Integer countValue;
}
