package io.growth.platform.profile.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SegmentBatchMatchResult {

    private Long segmentId;
    private List<SegmentMatchResult> results;
}
