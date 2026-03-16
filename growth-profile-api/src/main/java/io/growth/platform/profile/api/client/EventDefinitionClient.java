package io.growth.platform.profile.api.client;

import io.growth.platform.profile.api.dto.EventDefinitionDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "growth-profile", path = "/api/v1/profile/event-definitions")
public interface EventDefinitionClient {

    @GetMapping("/{eventName}")
    EventDefinitionDTO getByEventName(@PathVariable("eventName") String eventName);
}
