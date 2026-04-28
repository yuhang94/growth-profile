package io.growth.platform.profile.controller;

import io.growth.platform.common.core.result.PageResult;
import io.growth.platform.common.core.result.Result;
import io.growth.platform.profile.api.dto.*;
import io.growth.platform.profile.service.SegmentService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/profile/segments")
public class SegmentController {

    private final SegmentService segmentService;

    public SegmentController(SegmentService segmentService) {
        this.segmentService = segmentService;
    }

    @PostMapping
    public Result<SegmentDTO> create(@Valid @RequestBody SegmentCreateRequest request) {
        return Result.success(segmentService.create(request));
    }

    @PutMapping("/{id}")
    public Result<SegmentDTO> update(@PathVariable Long id,
                                     @Valid @RequestBody SegmentUpdateRequest request) {
        return Result.success(segmentService.update(id, request));
    }

    @GetMapping("/{id}")
    public Result<SegmentDTO> getById(@PathVariable Long id) {
        return Result.success(segmentService.getById(id));
    }

    @GetMapping
    public Result<PageResult<SegmentDTO>> page(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize) {
        return Result.success(segmentService.page(pageNum, pageSize));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        segmentService.delete(id);
        return Result.success();
    }

    @PostMapping("/preview")
    public Result<SegmentPreviewResult> preview(@Valid @RequestBody SegmentPreviewRequest request) {
        return Result.success(segmentService.preview(request));
    }

    @PostMapping("/{id}/compute")
    public Result<SegmentDTO> compute(@PathVariable Long id) {
        return Result.success(segmentService.compute(id));
    }

    @GetMapping("/{id}/users")
    public Result<PageResult<String>> getSegmentUsers(
            @PathVariable Long id,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize) {
        return Result.success(segmentService.getSegmentUsers(id, pageNum, pageSize));
    }

    @PostMapping("/match")
    public Result<SegmentMatchResult> match(@Valid @RequestBody SegmentMatchRequest request) {
        return Result.success(segmentService.match(request));
    }

    @PostMapping("/batch-match")
    public Result<SegmentBatchMatchResult> batchMatch(@Valid @RequestBody SegmentBatchMatchRequest request) {
        return Result.success(segmentService.batchMatch(request));
    }
}
