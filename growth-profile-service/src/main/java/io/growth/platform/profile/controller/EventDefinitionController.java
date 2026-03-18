package io.growth.platform.profile.controller;

import io.growth.platform.common.core.result.PageResult;
import io.growth.platform.common.core.result.Result;
import io.growth.platform.profile.api.dto.EventDefinitionCreateRequest;
import io.growth.platform.profile.api.dto.EventDefinitionDTO;
import io.growth.platform.profile.api.dto.EventDefinitionUpdateRequest;
import io.growth.platform.profile.api.dto.MqMappingTestRequest;
import io.growth.platform.profile.api.dto.MqMappingTestResult;
import io.growth.platform.profile.infrastructure.mq.DynamicMqConsumerManager;
import io.growth.platform.profile.service.EventDefinitionService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/v1/profile/event-definitions")
public class EventDefinitionController {

    private final EventDefinitionService eventDefinitionService;
    private final DynamicMqConsumerManager dynamicMqConsumerManager;

    public EventDefinitionController(EventDefinitionService eventDefinitionService,
                                     DynamicMqConsumerManager dynamicMqConsumerManager) {
        this.eventDefinitionService = eventDefinitionService;
        this.dynamicMqConsumerManager = dynamicMqConsumerManager;
    }

    @PostMapping
    public Result<EventDefinitionDTO> create(@Valid @RequestBody EventDefinitionCreateRequest request) {
        return Result.success(eventDefinitionService.create(request));
    }

    @PutMapping("/{eventName}")
    public Result<EventDefinitionDTO> update(@PathVariable String eventName,
                                             @Valid @RequestBody EventDefinitionUpdateRequest request) {
        return Result.success(eventDefinitionService.update(eventName, request));
    }

    @GetMapping("/{eventName}")
    public Result<EventDefinitionDTO> getByEventName(@PathVariable String eventName) {
        return Result.success(eventDefinitionService.getByEventName(eventName));
    }

    @GetMapping
    public Result<PageResult<EventDefinitionDTO>> page(
            @RequestParam(required = false) String eventType,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize) {
        return Result.success(eventDefinitionService.page(eventType, pageNum, pageSize));
    }

    @PutMapping("/{eventName}/status")
    public Result<Void> updateStatus(@PathVariable String eventName, @RequestBody Map<String, Integer> body) {
        eventDefinitionService.updateStatus(eventName, body.get("status"));
        return Result.success();
    }

    @PostMapping("/mq-mapping/test")
    public Result<MqMappingTestResult> testMqMapping(@Valid @RequestBody MqMappingTestRequest request) {
        return Result.success(eventDefinitionService.testMqMapping(request));
    }

    @GetMapping("/mq-consumers/status")
    public Result<Set<String>> getMqConsumerStatus() {
        return Result.success(dynamicMqConsumerManager.getActiveConsumerNames());
    }
}
