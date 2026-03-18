package io.growth.platform.profile.api.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class MqSourceConfigDTO {

    @NotBlank
    private String topic;

    private String tag;

    @NotBlank
    private String consumerGroup;

    @NotEmpty
    @Valid
    private List<FieldMapping> fieldMappings;
}
