package io.growth.platform.profile.api.dto;

import lombok.Data;

@Data
public class TagValueDTO {

    private String userId;
    private String tagKey;
    private String tagValue;
}
