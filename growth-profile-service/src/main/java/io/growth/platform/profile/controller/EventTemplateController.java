package io.growth.platform.profile.controller;

import io.growth.platform.common.core.result.Result;
import io.growth.platform.profile.api.dto.EventTemplateCreateRequest;
import io.growth.platform.profile.api.dto.EventTemplateDTO;
import io.growth.platform.profile.api.dto.EventTemplateUpdateRequest;
import io.growth.platform.profile.api.enums.EventType;
import io.growth.platform.profile.service.EventTemplateService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/profile/event-templates")
public class EventTemplateController {

    private final EventTemplateService eventTemplateService;

    public EventTemplateController(EventTemplateService eventTemplateService) {
        this.eventTemplateService = eventTemplateService;
    }

    @GetMapping
    public Result<List<EventTemplateDTO>> getByEventType(@RequestParam EventType eventType) {
        return Result.success(eventTemplateService.getByEventType(eventType));
    }

    @PostMapping
    public Result<EventTemplateDTO> create(@Valid @RequestBody EventTemplateCreateRequest request) {
        return Result.success(eventTemplateService.create(request));
    }

    @PutMapping("/{id}")
    public Result<EventTemplateDTO> update(@PathVariable Long id,
                                           @Valid @RequestBody EventTemplateUpdateRequest request) {
        return Result.success(eventTemplateService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        eventTemplateService.delete(id);
        return Result.success();
    }
}
