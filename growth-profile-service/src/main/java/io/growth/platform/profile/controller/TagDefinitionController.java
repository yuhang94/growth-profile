package io.growth.platform.profile.controller;

import io.growth.platform.common.core.result.PageResult;
import io.growth.platform.common.core.result.Result;
import io.growth.platform.profile.api.dto.TagDefinitionCreateRequest;
import io.growth.platform.profile.api.dto.TagDefinitionDTO;
import io.growth.platform.profile.api.dto.TagDefinitionUpdateRequest;
import io.growth.platform.profile.service.TagDefinitionService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/profile/tag-definitions")
public class TagDefinitionController {

    private final TagDefinitionService tagDefinitionService;

    public TagDefinitionController(TagDefinitionService tagDefinitionService) {
        this.tagDefinitionService = tagDefinitionService;
    }

    @PostMapping
    public Result<TagDefinitionDTO> create(@Valid @RequestBody TagDefinitionCreateRequest request) {
        return Result.success(tagDefinitionService.create(request));
    }

    @PutMapping("/{tagKey}")
    public Result<TagDefinitionDTO> update(@PathVariable String tagKey,
                                           @Valid @RequestBody TagDefinitionUpdateRequest request) {
        return Result.success(tagDefinitionService.update(tagKey, request));
    }

    @GetMapping("/{tagKey}")
    public Result<TagDefinitionDTO> getByTagKey(@PathVariable String tagKey) {
        return Result.success(tagDefinitionService.getByTagKey(tagKey));
    }

    @GetMapping
    public Result<PageResult<TagDefinitionDTO>> page(
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize) {
        return Result.success(tagDefinitionService.page(category, pageNum, pageSize));
    }

    @PutMapping("/{tagKey}/status")
    public Result<Void> updateStatus(@PathVariable String tagKey, @RequestBody Map<String, Integer> body) {
        tagDefinitionService.updateStatus(tagKey, body.get("status"));
        return Result.success();
    }
}
