package io.growth.platform.profile.api.dto;

import io.growth.platform.profile.api.enums.ExtractStrategy;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class FieldMapping {

    @NotBlank
    private String targetField;

    @NotNull
    private ExtractStrategy strategy;

    @NotBlank
    private String expression;
}
