package io.growth.platform.profile.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.growth.platform.profile.infrastructure.persistence.dataobject.EventTemplateDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface EventTemplateMapper extends BaseMapper<EventTemplateDO> {
}
