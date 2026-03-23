package io.growth.platform.profile.domain.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.growth.platform.profile.api.enums.BehaviorOperator;
import io.growth.platform.profile.api.enums.CompareOperator;
import io.growth.platform.profile.api.enums.ConditionOperator;
import lombok.Data;

import java.util.List;

@Data
public class SegmentCondition {

    private ConditionOperator operator;
    private List<SegmentCondition> children;

    // --- 标签条件字段 ---
    private String tagKey;
    private CompareOperator compareOp;
    private List<String> values;

    // --- 行为条件字段 ---
    private BehaviorOperator behaviorOp;
    private String eventName;
    private List<EventPropertyFilter> propertyFilters;
    private String timeRangeStart;
    private String timeRangeEnd;
    private CompareOperator countOp;
    private Integer countValue;

    @JsonIgnore
    public boolean isLeaf() {
        return tagKey != null || eventName != null;
    }

    @JsonIgnore
    public boolean isTagCondition() {
        return tagKey != null;
    }

    @JsonIgnore
    public boolean isBehaviorCondition() {
        return eventName != null;
    }
}
