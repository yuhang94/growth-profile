package io.growth.platform.profile.api.client;

import io.growth.platform.profile.api.dto.TagValueDTO;
import io.growth.platform.profile.api.dto.UserTagsDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "growth-profile", path = "/api/v1/profile/tag-values")
public interface TagValueClient {

    @GetMapping
    TagValueDTO getTagValue(@RequestParam("userId") String userId, @RequestParam("tagKey") String tagKey);

    @GetMapping("/user/{userId}")
    UserTagsDTO getUserTags(@PathVariable("userId") String userId);
}
