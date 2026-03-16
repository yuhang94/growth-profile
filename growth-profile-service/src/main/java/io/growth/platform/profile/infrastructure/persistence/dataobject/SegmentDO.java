package io.growth.platform.profile.infrastructure.persistence.dataobject;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("gp_profile_segment")
public class SegmentDO {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String segmentName;
    private String description;
    private String conditionJson;
    private Integer status;
    private Long lastUserCount;
    private LocalDateTime lastComputedTime;
    private String createdBy;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedTime;
}
