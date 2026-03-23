package io.growth.platform.profile.controller;

import io.growth.platform.common.core.result.Result;
import io.growth.platform.profile.api.dto.SegmentTemplateCreateRequest;
import io.growth.platform.profile.api.dto.SegmentTemplateDTO;
import io.growth.platform.profile.service.SegmentTemplateService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/profile/segment-templates")
public class SegmentTemplateController {

    private final SegmentTemplateService segmentTemplateService;

    public SegmentTemplateController(SegmentTemplateService segmentTemplateService) {
        this.segmentTemplateService = segmentTemplateService;
    }

    @GetMapping
    public Result<List<SegmentTemplateDTO>> listAll() {
        return Result.success(segmentTemplateService.listAll());
    }

    @PostMapping
    public Result<SegmentTemplateDTO> create(@Valid @RequestBody SegmentTemplateCreateRequest request) {
        return Result.success(segmentTemplateService.create(request));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        segmentTemplateService.delete(id);
        return Result.success();
    }
}
