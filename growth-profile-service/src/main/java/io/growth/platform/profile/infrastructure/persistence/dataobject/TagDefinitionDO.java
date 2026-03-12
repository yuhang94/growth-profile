package io.growth.platform.profile.infrastructure.persistence.dataobject;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("gp_profile_tag_definition")
public class TagDefinitionDO {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String tagKey;

    private String tagName;

    private String tagType;

    private String category;

    private String description;

    private String enumValues;

    private Integer status;

    private String createdBy;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedTime;
}
