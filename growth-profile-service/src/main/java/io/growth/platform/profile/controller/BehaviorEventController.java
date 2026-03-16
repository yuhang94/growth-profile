package io.growth.platform.profile.controller;

import io.growth.platform.common.core.result.Result;
import io.growth.platform.profile.api.dto.BehaviorEventBatchRequest;
import io.growth.platform.profile.api.dto.BehaviorEventDTO;
import io.growth.platform.profile.api.dto.BehaviorEventRequest;
import io.growth.platform.profile.service.BehaviorEventService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/profile/behavior-events")
public class BehaviorEventController {

    private final BehaviorEventService behaviorEventService;

    public BehaviorEventController(BehaviorEventService behaviorEventService) {
        this.behaviorEventService = behaviorEventService;
    }

    @PostMapping
    public Result<Void> report(@Valid @RequestBody BehaviorEventRequest request) {
        behaviorEventService.report(request);
        return Result.success();
    }

    @PostMapping("/batch")
    public Result<Void> batchReport(@Valid @RequestBody BehaviorEventBatchRequest request) {
        behaviorEventService.batchReport(request);
        return Result.success();
    }

    @GetMapping
    public Result<List<BehaviorEventDTO>> query(
            @RequestParam(required = false) String userId,
            @RequestParam(required = false) String eventName,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize) {
        return Result.success(behaviorEventService.query(userId, eventName, startTime, endTime, pageNum, pageSize));
    }

    @GetMapping("/user/{userId}")
    public Result<List<BehaviorEventDTO>> getUserRecentEvents(
            @PathVariable String userId,
            @RequestParam(defaultValue = "50") int limit) {
        return Result.success(behaviorEventService.getUserRecentEvents(userId, limit));
    }
}
