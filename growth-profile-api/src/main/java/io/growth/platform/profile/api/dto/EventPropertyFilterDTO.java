package io.growth.platform.profile.api.dto;

import io.growth.platform.profile.api.enums.CompareOperator;
import lombok.Data;

import java.util.List;

@Data
public class EventPropertyFilterDTO {

    private String propertyKey;
    private CompareOperator compareOp;
    private List<String> values;
}
