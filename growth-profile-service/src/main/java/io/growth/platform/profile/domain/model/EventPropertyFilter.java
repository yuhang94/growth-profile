package io.growth.platform.profile.domain.model;

import io.growth.platform.profile.api.enums.CompareOperator;
import lombok.Data;

import java.util.List;

@Data
public class EventPropertyFilter {

    private String propertyKey;
    private CompareOperator compareOp;
    private List<String> values;
}
