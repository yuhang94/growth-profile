package io.growth.platform.profile.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.growth.platform.profile.infrastructure.persistence.dataobject.TagDefinitionDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface TagDefinitionMapper extends BaseMapper<TagDefinitionDO> {
}
