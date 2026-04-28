package io.growth.platform.profile.api.client;

import io.growth.platform.profile.api.dto.EventDefinitionDTO;
import io.growth.platform.profile.api.enums.UsageChannel;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "growth-profile", path = "/api/v1/profile/event-definitions")
public interface EventDefinitionClient {

    @GetMapping("/{eventName}")
    EventDefinitionDTO getByEventName(@PathVariable("eventName") String eventName);

    @GetMapping
    List<EventDefinitionDTO> page(
            @RequestParam(value = "eventType", required = false) String eventType,
            @RequestParam(value = "usageChannel", required = false) UsageChannel usageChannel,
            @RequestParam(value = "pageNum", defaultValue = "1") int pageNum,
            @RequestParam(value = "pageSize", defaultValue = "20") int pageSize);
}
