package io.growth.platform.profile.api.dto;

import io.growth.platform.profile.api.enums.TagType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class TagDefinitionUpdateRequest {

    @NotBlank(message = "标签名称不能为空")
    @Size(max = 128, message = "标签名称最长128字符")
    private String tagName;

    @NotNull(message = "标签值类型不能为空")
    private TagType tagType;

    @Size(max = 64, message = "分类最长64字符")
    private String category;

    @Size(max = 512, message = "描述最长512字符")
    private String description;

    private List<String> enumValues;
}
