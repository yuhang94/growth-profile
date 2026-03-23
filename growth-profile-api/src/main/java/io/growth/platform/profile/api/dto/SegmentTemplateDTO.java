package io.growth.platform.profile.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SegmentTemplateDTO {

    private Long id;
    private String templateKey;
    private String title;
    private String description;
    private List<ConditionSlotDTO> slots;
    private int sortOrder;
    private boolean builtIn;
}
