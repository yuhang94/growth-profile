package io.growth.platform.profile.api.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class MqMappingTestRequest {

    @NotBlank
    private String sampleMessage;

    @NotEmpty
    @Valid
    private List<FieldMapping> fieldMappings;
}
