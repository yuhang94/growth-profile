package io.growth.platform.profile.api.client;

import io.growth.platform.profile.api.dto.SegmentDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "growth-profile", path = "/api/v1/profile/segments")
public interface SegmentClient {

    @GetMapping("/{id}")
    SegmentDTO getById(@PathVariable("id") Long id);
}
