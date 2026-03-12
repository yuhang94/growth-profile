package io.growth.platform.profile.api.client;

import io.growth.platform.profile.api.dto.TagDefinitionDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "growth-profile", path = "/api/v1/profile/tag-definitions")
public interface TagDefinitionClient {

    @GetMapping("/{tagKey}")
    TagDefinitionDTO getByTagKey(@PathVariable("tagKey") String tagKey);
}
