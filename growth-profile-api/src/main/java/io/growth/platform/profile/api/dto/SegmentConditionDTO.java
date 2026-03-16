package io.growth.platform.profile.api.dto;

import io.growth.platform.profile.api.enums.CompareOperator;
import io.growth.platform.profile.api.enums.ConditionOperator;
import lombok.Data;

import java.util.List;

@Data
public class SegmentConditionDTO {

    private ConditionOperator operator;
    private List<SegmentConditionDTO> children;

    private String tagKey;
    private CompareOperator compareOp;
    private List<String> values;
}
