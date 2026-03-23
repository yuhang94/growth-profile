package io.growth.platform.profile.infrastructure.persistence.dataobject;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("gp_profile_segment_template")
public class SegmentTemplateDO {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String templateKey;
    private String title;
    private String description;
    private String slots;
    private int sortOrder;
    @TableField("is_built_in")
    private boolean builtIn;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedTime;
}
