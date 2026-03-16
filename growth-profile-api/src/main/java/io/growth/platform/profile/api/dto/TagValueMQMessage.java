package io.growth.platform.profile.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TagValueMQMessage {

    private String userId;
    private String tagKey;
    private String tagValue;
}
