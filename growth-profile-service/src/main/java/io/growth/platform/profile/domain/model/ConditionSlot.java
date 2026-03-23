package io.growth.platform.profile.domain.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ConditionSlot {

    private String label;
    private List<String> eventTypes;
    private int defaultDays;
    private int defaultCount;
}
