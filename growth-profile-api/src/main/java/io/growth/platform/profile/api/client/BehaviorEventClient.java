package io.growth.platform.profile.api.client;

import io.growth.platform.profile.api.dto.BehaviorEventDTO;
import io.growth.platform.profile.api.dto.BehaviorEventRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(name = "growth-profile", path = "/api/v1/profile/behavior-events")
public interface BehaviorEventClient {

    @PostMapping
    void report(@RequestBody BehaviorEventRequest request);

    @GetMapping("/user/{userId}")
    List<BehaviorEventDTO> getUserEvents(@PathVariable("userId") String userId);
}
