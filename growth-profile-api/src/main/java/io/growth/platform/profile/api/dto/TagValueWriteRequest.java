package io.growth.platform.profile.api.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class TagValueWriteRequest {

    @NotBlank(message = "用户ID不能为空")
    private String userId;

    @NotBlank(message = "标签key不能为空")
    private String tagKey;

    @NotBlank(message = "标签值不能为空")
    private String tagValue;
}
