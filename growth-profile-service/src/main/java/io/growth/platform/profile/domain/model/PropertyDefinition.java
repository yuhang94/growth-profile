package io.growth.platform.profile.domain.model;

import lombok.Data;

@Data
public class PropertyDefinition {

    private String propertyName;
    private String propertyType;
    private String defaultValue;
    private String displayName;
    private boolean required;
}
