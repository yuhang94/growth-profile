package io.growth.platform.profile.domain.model;

import io.growth.platform.profile.api.dto.FieldMapping;
import lombok.Data;

import java.util.List;

@Data
public class MqSourceConfig {

    private String topic;
    private String tag;
    private String consumerGroup;
    private List<FieldMapping> fieldMappings;
}
