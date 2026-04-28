package io.growth.platform.profile.api.client;

import io.growth.platform.profile.api.dto.SegmentBatchMatchRequest;
import io.growth.platform.profile.api.dto.SegmentBatchMatchResult;
import io.growth.platform.profile.api.dto.SegmentDTO;
import io.growth.platform.profile.api.dto.SegmentMatchRequest;
import io.growth.platform.profile.api.dto.SegmentMatchResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "growth-profile", path = "/api/v1/profile/segments")
public interface SegmentClient {

    @GetMapping("/{id}")
    SegmentDTO getById(@PathVariable("id") Long id);

    @PostMapping("/match")
    SegmentMatchResult match(@RequestBody SegmentMatchRequest request);

    @PostMapping("/batch-match")
    SegmentBatchMatchResult batchMatch(@RequestBody SegmentBatchMatchRequest request);
}
