package io.growth.platform.profile.api.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class TagValueBatchWriteRequest {

    @NotEmpty(message = "标签值列表不能为空")
    @Valid
    private List<TagValueWriteRequest> items;
}
