package io.growth.platform.profile.api.dto;

import lombok.Data;

@Data
public class PropertyDefinitionDTO {

    private String propertyName;
    private String propertyType;
    private String defaultValue;
    private String displayName;
    private boolean required;
}
