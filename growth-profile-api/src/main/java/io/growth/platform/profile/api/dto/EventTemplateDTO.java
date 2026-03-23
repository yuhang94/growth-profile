package io.growth.platform.profile.api.dto;

import io.growth.platform.profile.api.enums.EventType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EventTemplateDTO {

    private Long id;

    private EventType eventType;

    private String templateName;

    private String description;

    private String sampleJson;
}
