package io.growth.platform.profile.domain.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.growth.platform.profile.api.enums.CompareOperator;
import io.growth.platform.profile.api.enums.ConditionOperator;
import lombok.Data;

import java.util.List;

@Data
public class SegmentCondition {

    private ConditionOperator operator;
    private List<SegmentCondition> children;

    private String tagKey;
    private CompareOperator compareOp;
    private List<String> values;

    @JsonIgnore
    public boolean isLeaf() {
        return tagKey != null;
    }
}
