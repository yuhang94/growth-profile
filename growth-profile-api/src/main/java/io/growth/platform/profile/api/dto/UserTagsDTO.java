package io.growth.platform.profile.api.dto;

import lombok.Data;

import java.util.Map;

@Data
public class UserTagsDTO {

    private String userId;
    private Map<String, String> tags;
}
