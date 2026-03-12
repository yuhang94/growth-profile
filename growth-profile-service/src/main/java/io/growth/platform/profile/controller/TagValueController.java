package io.growth.platform.profile.controller;

import io.growth.platform.common.core.result.Result;
import io.growth.platform.profile.api.dto.*;
import io.growth.platform.profile.service.TagValueService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/profile/tag-values")
public class TagValueController {

    private final TagValueService tagValueService;

    public TagValueController(TagValueService tagValueService) {
        this.tagValueService = tagValueService;
    }

    @PostMapping
    public Result<Void> write(@Valid @RequestBody TagValueWriteRequest request) {
        tagValueService.write(request);
        return Result.success();
    }

    @PostMapping("/batch")
    public Result<Void> batchWrite(@Valid @RequestBody TagValueBatchWriteRequest request) {
        tagValueService.batchWrite(request);
        return Result.success();
    }

    @GetMapping
    public Result<TagValueDTO> getTagValue(@RequestParam String userId, @RequestParam String tagKey) {
        return Result.success(tagValueService.getTagValue(userId, tagKey));
    }

    @GetMapping("/user/{userId}")
    public Result<UserTagsDTO> getUserTags(@PathVariable String userId) {
        return Result.success(tagValueService.getUserTags(userId));
    }

    @DeleteMapping
    public Result<Void> delete(@RequestParam String userId, @RequestParam String tagKey) {
        tagValueService.delete(userId, tagKey);
        return Result.success();
    }
}
