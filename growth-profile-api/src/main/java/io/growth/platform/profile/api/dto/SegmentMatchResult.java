package io.growth.platform.profile.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SegmentMatchResult {

    private Long segmentId;
    private String userId;
    private Boolean matched;
    private Long version;
    private String reason;
}
