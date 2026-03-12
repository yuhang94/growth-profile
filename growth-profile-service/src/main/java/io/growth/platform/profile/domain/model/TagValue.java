package io.growth.platform.profile.domain.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TagValue {

    private String userId;
    private String tagKey;
    private String tagValue;
}
