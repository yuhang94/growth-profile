package io.growth.platform.profile.api.dto;

import io.growth.platform.profile.api.enums.TagType;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class TagDefinitionDTO {

    private Long id;
    private String tagKey;
    private String tagName;
    private TagType tagType;
    private String category;
    private String description;
    private List<String> enumValues;
    private Integer status;
    private String createdBy;
    private LocalDateTime createdTime;
    private LocalDateTime updatedTime;
}
