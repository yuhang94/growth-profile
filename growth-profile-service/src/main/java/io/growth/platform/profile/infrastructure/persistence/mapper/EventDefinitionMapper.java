package io.growth.platform.profile.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.growth.platform.profile.infrastructure.persistence.dataobject.EventDefinitionDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface EventDefinitionMapper extends BaseMapper<EventDefinitionDO> {
}
